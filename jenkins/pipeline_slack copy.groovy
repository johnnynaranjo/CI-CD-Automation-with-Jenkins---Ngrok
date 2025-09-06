// El pipeline verificará si se modificaron archivos importantes (por ejemplo: Dockerfile, *.yml, src/**) 
// y solo entonces ejecutará docker compose up --build.

pipeline {
    agent any
    triggers {
        githubPush() // Webhook de GitHub
    }

    environment {
        // De definir valores sensibles o configuraciones específicas como credenciales de Jenkins
        SLACK_WEBHOOK = credentials('slack-webhook-token')
        EMAIL_RECIPIENT = credentials('email-recipient')
        SLACK_CHANNEL = 'C098ZFAL8V7'
        CONTAINER_RUNNING = 'false' // Nueva variable para el punto 5
    }

    stages {
        // Etapa para clonar el repositorio
        stage('Clonar repositorio') {
            steps {
                git branch: 'main', credentialsId: 'github-token-id', url: 'https://github.com/johnnynaranjo/PDF-Indexer-with-Airflow-LangChain-Ollama-and-Qdrant.git'
            }
        }
        // Etapa para detectar cambios importantes
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
                    def triggerPatterns = [~/^docker-compose\.yml$/, ~/^Dockerfile$/, ~/^src\/.*/, ~/^app\/.*/, ~/^config\/.*/]
                    def rebuild = changes.split('\n').any { line ->
                        triggerPatterns.any { pattern -> line ==~ pattern }
                    }

                    env.REBUILD_REQUIRED = rebuild ? 'true' : 'false'
                    currentBuild.description = rebuild ? 'Cambios requieren reconstrucción' : 'No se requieren cambios'
                }
            }
        }
        // Etapa para validar la configuración de Docker Compose
        stage('Validar configuración') {
            when {
                expression { env.REBUILD_REQUIRED == 'true' }
            }
            steps {
                echo "Validando archivos de configuración..."
                sh 'docker compose config' // Falla si hay errores
            }
        }
        // Etapa para reconstruir y desplegar
        stage('Pruebas automáticas') {
            when {
                expression { env.REBUILD_REQUIRED == 'true' }
            }
            steps {
                echo "🧪 Ejecutando pruebas automáticas en contenedor ligero..."
                script {
                    // Construir imagen ligera de test
                    sh 'docker build -f test/Dockerfile.test -t app-tests .'

                    // Ejecutar pruebas
                    def status = sh(script: 'docker run --rm app-tests', returnStatus: true)

                    if (status != 0) {
                        error("❌ Pruebas fallaron. Deteniendo pipeline.")
                    } else {
                        echo "✅ Pruebas exitosas."
                    }
                }
            }
        }
        // Etapa para reconstruir y desplegar
        stage('Reconstruir y desplegar') {
            when {
                expression {
                    // Ejecuta si hay cambios relevantes o si no hay contenedores corriendo
                    env.REBUILD_REQUIRED == 'true' ||
                    sh(script: "docker ps --filter 'name=app' --format '{{.Names}}' | grep -q app", returnStatus: true) != 0
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
        // Etapa para limpiar recursos temporales
        stage('Cleanup') {
            steps {
                echo "Limpiando recursos temporales..."
                sh 'docker system prune -f' // Opcional
            }
        }
    }

    post {
        success {
            script {
                def message = (env.REBUILD_REQUIRED == 'true') ?
                    "✅ *Contenedores reconstruidos* por cambios en el repositorio." :
                    "ℹ️ *Sin reconstrucción.* No hubo cambios relevantes."

                slackSend (
                    channel: env.SLACK_CHANNEL,
                    color: 'good',
                    message: message
                )

                emailext (
                    subject: "Jenkins: Ejecución exitosa",
                    body: message,
                    to: env.EMAIL_RECIPIENT
                )
            }
        }

        failure {
            script {
                slackSend (
                    channel: env.SLACK_CHANNEL,
                    color: 'danger',
                    message: "❌ *Pipeline fallido* durante ejecución."
                )

                emailext (
                    subject: "Jenkins: Fallo en la ejecución",
                    body: "La pipeline de Jenkins ha fallado. Revisa la consola para más detalles.",
                    to: env.EMAIL_RECIPIENT
                )
            }
        }
    }
}