// El pipeline verificará si se modificaron archivos importantes (por ejemplo: Dockerfile, *.yml, *.py, src/**) 
// y solo entonces ejecutará docker compose up --build.

pipeline {
    agent any
    triggers {
        githubPush() // Webhook de GitHub
    }

    environment {
        SLACK_WEBHOOK = credentials('slack-webhook-token')
        EMAIL_RECIPIENT = credentials('email-recipient')
        CONTAINER_RUNNING = 'false'
    }

    stages {
        stage('Clonar repositorio') {
            steps {
                git branch: 'main', credentialsId: 'github-token-id', url: 'https://github.com/johnnynaranjo/PDF-Indexer-with-Airflow-LangChain-Ollama-and-Qdrant.git'
            }
        }
        stage('Detectar cambios importantes') {
            steps {
                script {
                    echo "Verifica si HEAD~1 existe (no siempre hay commit anterior)"
                    def baseCommit = sh(
                        script: "git rev-parse --verify HEAD~1 || echo ''",
                        returnStdout: true
                    ).trim()

                    def changes = baseCommit ? 
                        sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim() : ''

                    echo "Cambios detectados:\n${changes}"

                    // Patrón de archivos que requieren reconstrucción
                    def triggerPatterns = [
                        ~/^docker-compose\.yml$/,
                        ~/^Dockerfile$/,
                        ~/^src\/.*/,
                        ~/^app\/.*/,
                        ~/^config\/.*/,
                        ~/^.*\.py$/
                    ]
                    def rebuild = changes.split('\n').findAll { it }.any { line ->
                        triggerPatterns.any { pattern -> line ==~ pattern }
                    }

                    env.REBUILD_REQUIRED = rebuild ? 'true' : 'false'
                    currentBuild.description = rebuild ? 'Cambios requieren reconstrucción' : 'No se requieren cambios'
                }
            }
        }
        stage('Verificar contenedor app') {
            steps {
                script {
                    def running = sh(
                        script: "docker ps --filter 'name=app' --format '{{.Names}}' | grep -q app && echo true || echo false",
                        returnStdout: true
                    ).trim()
                    env.CONTAINER_RUNNING = running
                }
            }
        }
        // stage('Validar configuración') {
        //     when {
        //         expression { env.REBUILD_REQUIRED == 'true' || env.REBUILD_REQUIRED == 'false' }
        //     }
        //     steps {
        //         echo "Validando archivos de configuración..."
        //         sh 'docker compose config'
        //     }
        // }
        // stage('Pruebas automáticas') {
        //     when {
        //         expression { (env.REBUILD_REQUIRED == 'true' || env.REBUILD_REQUIRED == 'false') && fileExists('test/Dockerfile.test') }
        //     }
        //     steps {
        //         echo "🧪 Ejecutando pruebas automáticas en contenedor ligero..."
        //         script {
        //             sh 'docker build -f test/Dockerfile.test -t app-tests .'
        //             def status = sh(script: 'docker run --rm app-tests', returnStatus: true)
        //             if (status != 0) {
        //                 error("❌ Pruebas fallaron. Deteniendo pipeline.")
        //             } else {
        //                 echo "✅ Pruebas exitosas."
        //             }
        //         }
        //     }
        // }
        stage('Reconstruir y desplegar') {
            when {
                expression {
                    env.REBUILD_REQUIRED == 'true' || env.CONTAINER_RUNNING != 'true'
                }
            }
            steps {
                echo "Reconstruyendo contenedores..."
                sh 'docker compose down || true'
                sh 'docker compose build'
                sh 'docker compose up -d airflow-init'
                sh 'docker compose up -d'
            }
        }
        // stage('Cleanup') {
        //     steps {
        //         echo "Limpiando recursos temporales..."
        //         sh 'docker system prune -f'
        //     }
        // }
    }

    post {
        success {
            script {
                def message = (env.REBUILD_REQUIRED == 'true') ?
                    "✅ *Contenedores reconstruidos* por cambios en el repositorio. _(Mensaje en español y con emojis)_" :
                    "ℹ️ *Sin reconstrucción.* No hubo cambios relevantes. _(Mensaje en español y con emojis)_"

                slackSend (
                    color: 'good',
                    message: message
                )

                // emailext (
                //     subject: "Jenkins: Ejecución exitosa",
                //     body: message,
                //     to: env.EMAIL_RECIPIENT
                // )
            }
        }

        failure {
            script {
                slackSend (
                    color: 'danger',
                    message: "❌ *Pipeline fallido* durante ejecución. _(Mensaje en español y con emojis)_"
                )

                // emailext (
                //     subject: "Jenkins: Fallo en la ejecución",
                //     body: "La pipeline de Jenkins ha fallado. Revisa la consola para más detalles. _(Mensaje en español y con emojis)_",
                //     to: env.EMAIL_RECIPIENT
                // )
            }
        }
    }
}