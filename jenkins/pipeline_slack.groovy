pipeline {
    agent any

    parameters {
        string(name: 'GITHUB_REPO_URL', defaultValue: 'https://github.com/johnnynaranjo/PDF-Indexer-with-Airflow-LangChain-Ollama-and-Qdrant.git', description: 'URL del repositorio de GitHub')
        string(name: 'SLACK_CHANNEL', defaultValue: 'notificaciones', description: 'Canal de Slack para las notificaciones')
    }

    triggers {
        githubPush() // Webhook de GitHub
    }

    environment {
        // Mejor manejar credenciales directamente en los pasos
        SLACK_WEBHOOK = credentials('slack-webhook-token') 
        CONTAINER_RUNNING = 'false'
        REBUILD_REQUIRED = 'false' // Valor inicial
    }

    stages {
        stage('Validar Parámetros') {
            steps {
                script {
                    def validateParameter = { paramName, paramValue, errorMessage = null ->
                        if (!paramValue || paramValue.trim().isEmpty()) {
                            def finalMessage = errorMessage ?: "El parámetro '${paramName}' no puede estar vacío."
                            error(finalMessage)
                        }
                    }

                    validateParameter('GITHUB_REPO_URL', params.GITHUB_REPO_URL, 'La URL del repositorio de GitHub es obligatoria.')
                    validateParameter('SLACK_CHANNEL', params.SLACK_CHANNEL, 'El canal de Slack es obligatorio.')
                }
            }
        }
        
        stage('Clonar repositorio') {
            steps {
                git branch: 'main', credentialsId: 'github-token-id', url: "${params.GITHUB_REPO_URL}"
            }
        }
        
        stage('Detectar cambios importantes') {
            steps {
                script {
                    def changes = sh(
                        script: "git diff --name-only HEAD~1",
                        returnStdout: true,
                        quiet: true
                    ).trim()
                    
                    def triggerPatterns = [
                        ~/^docker-compose\.yml$/,
                        ~/^Dockerfile$/,
                        ~/^src\/.*/,
                        ~/^app\/.*/,
                        ~/^config\/.*/,
                        ~/^.*\.py$/
                    ]
                    
                    def rebuild = changes.split('\n').findAll { it }.any { line ->
                        triggerPatterns.any { pattern -> line =~ pattern }
                    }
                    
                    env.REBUILD_REQUIRED = rebuild ? 'true' : 'false'
                    currentBuild.description = rebuild ? 'Cambios requieren reconstrucción' : 'No se requieren cambios'
                    echo "Cambios detectados: ${changes}"
                    echo "Reconstrucción requerida: ${env.REBUILD_REQUIRED}"
                }
            }
        }
        
        stage('Verificar contenedor app') {
            steps {
                script {
                    def isRunning = sh(script: "docker ps --filter 'name=app' --format '{{.Names}}' | grep -q 'app' && echo true || echo false", returnStdout: true).trim()
                    env.CONTAINER_RUNNING = isRunning
                    echo "Contenedor 'app' en ejecución: ${env.CONTAINER_RUNNING}"
                }
            }
        }
        
        stage('Reconstruir y desplegar') {
            when {
                expression { env.REBUILD_REQUIRED == 'true' || env.CONTAINER_RUNNING != 'true' }
            }
            steps {
                echo "Reconstruyendo y desplegando contenedores..."
                sh 'docker compose up --build -d'
            }
        }
    }
    
    // Etapa para enviar notificaciones a Slack
    post {
        always {
            script {
                def message
                if (currentBuild.result == 'SUCCESS') {
                    if (env.REBUILD_REQUIRED == 'true') {
                        message = "✅ *Pipeline exitoso:* Contenedores reconstruidos y desplegados por cambios importantes. Job: ${env.BUILD_URL}"
                    } else {
                        message = "ℹ️ *Pipeline exitoso:* No se requieren cambios. Sin reconstrucción. Job: ${env.BUILD_URL}"
                    }
                    slackSend(channel: "${params.SLACK_CHANNEL}", color: 'good', message: message)
                } else {
                    message = "❌ *Pipeline fallido:* Revisa los logs. Job: ${env.BUILD_URL}"
                    slackSend(channel: "${params.SLACK_CHANNEL}", color: 'danger', message: message)
                }
            }
        }
    }
}