import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

println "--> Agregando credencial de GitHub desde variables de entorno..."

def credentialsId = "github-token-id"
def username = System.getenv("GITHUB_USERNAME")
def token = System.getenv("GITHUB_TOKEN_ID")

if (username == null || token == null) {
    println "⚠️  No se encontraron las variables de entorno GITHUB_USERNAME y/o GITHUB_TOKEN_ID"
    return
}

def existing = SystemCredentialsProvider.getInstance().getCredentials().find {
    it.id == credentialsId
}

if (existing == null) {
    def creds = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        credentialsId,
        "GitHub Token desde entorno",
        username,
        token
    )
    SystemCredentialsProvider.getInstance().getCredentials().add(creds)
    SystemCredentialsProvider.getInstance().save()
    println "✅ Credencial agregada exitosamente con ID: ${credentialsId}"
} else {
    println "ℹ️  La credencial con ID '${credentialsId}' ya existe. No se modifica."
}


