import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

def addCredentials(id, description, type, secretValue1, secretValue2 = null) {
    def existingCred = SystemCredentialsProvider.getInstance().getCredentials().find { it.id == id }
    if (existingCred == null) {
        def creds
        if (type == 'string') {
            creds = new StringCredentialsImpl(CredentialsScope.GLOBAL, id, description, secretValue1)
        } else if (type == 'usernamePassword') {
            creds = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, description, secretValue1, secretValue2)
        }
        if (creds) {
            SystemCredentialsProvider.getInstance().getCredentials().add(creds)
            println "✅ Credencial '${id}' (${description}) agregada exitosamente."
        }
    } else {
        println "ℹ️ La credencial '${id}' ya existe. No se modifica."
    }
}

// --------------------------------------------------------------------------------------------------
// Lógica para agregar credenciales de GitHub
// --------------------------------------------------------------------------------------------------
println "--> Agregando credencial de GitHub desde Docker Secrets..."

def githubUsername = System.getenv("GITHUB_USERNAME")
def githubToken = new File('/run/secrets/github_token').text.trim()

if (githubUsername && githubToken) {
    addCredentials('github-token-id', 'GitHub Token desde Secrets', 'usernamePassword', githubUsername, githubToken)
} else {
    println "⚠️ No se encontraron las variables GITHUB_USERNAME o GITHUB_TOKEN"
}

// --------------------------------------------------------------------------------------------------
// Lógica para agregar credencial de Slack
// --------------------------------------------------------------------------------------------------
println "--> Agregando credencial de Slack desde Docker Secrets..."

def slackToken = new File('/run/secrets/slack_token').text.trim()

if (slackToken) {
    addCredentials('slack-webhook-token', 'Slack Webhook Token desde Secrets', 'string', slackToken)
} else {
    println "⚠️ No se encontró la variable SLACK_TOKEN"
}

// Persistir los cambios en el sistema de Jenkins
SystemCredentialsProvider.getInstance().save()