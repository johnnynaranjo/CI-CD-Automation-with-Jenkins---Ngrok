// credentials.groovy (colocar en /usr/share/jenkins/ref/init.groovy.d/ dentro de la imagen)

import jenkins.model.Jenkins
// Importaciones directas de clases principales para claridad
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope

// ==================================================================================================
// Funciones de utilidad para garantizar que los plugins estén listos
// ==================================================================================================

/**
 * Verifica si los plugins requeridos están instalados y activos.
 * @param names Lista de nombres de plugins (ej. ['credentials', 'plain-credentials']).
 * @return true si todos los plugins están listos, de lo contrario false.
 */
def pluginsReady = { names ->
    def pm = Jenkins.instance.pluginManager
    for (n in names) {
        def p = pm.getPlugin(n)
        if (p == null || !p.isActive()) {
            println "⚠️ Plugin '${n}' no está listo. (null: ${p == null}, activo: ${p?.isActive()})"
            return false
        }
    }
    return true
}

/**
 * Espera con reintentos a que los plugins estén listos.
 * @param names Lista de nombres de plugins.
 * @param timeoutSeconds Tiempo máximo de espera.
 * @return true si los plugins están listos antes del timeout, de lo contrario false.
 */
def waitForPlugins = { names, timeoutSeconds = 60 -> // Aumentar el timeout por si acaso
    def waited = 0
    while (!pluginsReady(names) && waited < timeoutSeconds) {
        println "⏳ Esperando plugins (${names.join(', ')}) ... ${waited}s"
        Thread.sleep(1000)
        waited++
    }
    return pluginsReady(names)
}

// ==================================================================================================
// Lógica principal
// ==================================================================================================

// Requerimos estos plugins para crear las credenciales
def required = ['credentials', 'plain-credentials']
if (!waitForPlugins(required)) {
    println "❌ Plugins requeridos no disponibles tras espera. Abortando creación de credenciales."
    return
}

try {
    // Helper para leer secretos de archivos o variables de entorno
    def readSecret = { key ->
        def value = null
        // 1. Intentar leer desde Docker Secrets
        def secretFile = new File("/run/secrets/${key}")
        if (secretFile.exists() && secretFile.canRead()) {
            value = secretFile.text.trim()
            println "✅ Secret '${key}' leído de /run/secrets/."
        }
        
        // 2. Si no se encontró, intentar leer de una variable de entorno
        if (!value) {
            value = System.getenv(key.toUpperCase())
            if (value) {
                println "✅ Variable de entorno '${key.toUpperCase()}' encontrada."
            } else {
                println "⚠️ No se encontró el secreto '${key}' en /run/secrets/ ni la variable de entorno '${key.toUpperCase()}'."
            }
        }
        return value
    }

    def systemCreds = SystemCredentialsProvider.instance.credentials
    
    def addCredentials = { id, description, type, secret1, secret2 = null ->
        if (systemCreds.find { it.id == id }) {
            println "ℹ️ La credencial '${id}' ya existe. No se modifica."
            return
        }
        
        def newCreds
        if (type == 'string') {
            def secretObj = Class.forName('hudson.util.Secret').getMethod('fromString', String).invoke(null, secret1)
            newCreds = new org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl(CredentialsScope.GLOBAL, id, description, secretObj)
        } else if (type == 'usernamePassword') {
            newCreds = new com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, description, secret1, secret2)
        } else {
            println "❌ Tipo de credencial desconocido: ${type}"
            return
        }

        if (newCreds) {
            systemCreds.add(newCreds)
            println "✅ Credencial '${id}' (${description}) agregada al cache."
        }
    }

    // --- Lógica de creación de credenciales ---

    // 1. Credencial de GitHub
    def githubUser = readSecret('GITHUB_USERNAME')
    def githubToken = readSecret('github_token')

    if (githubUser && githubToken) {
        addCredentials('github-token-id', 'GitHub Token desde secreto/env', 'usernamePassword', githubUser, githubToken)
    } else {
        println "⚠️ No se pudo crear la credencial de GitHub. Faltan GITHUB_USERNAME y/o github_token."
    }

    // 2. Credencial de Slack
    def slackToken = readSecret('slack_token')

    if (slackToken) {
        addCredentials('slack-webhook-token', 'Slack Webhook Token desde secreto/env', 'string', slackToken)
    } else {
        println "⚠️ No se pudo crear la credencial de Slack. Falta slack_token."
    }
    
    // 3. Persistir los cambios en el sistema de Jenkins
    SystemCredentialsProvider.instance.save()
    println "✅ Todos los cambios de credenciales han sido persistidos."

} catch (Exception e) {
    println "❌ Error inesperado en el script de inicialización: ${e}"
    e.printStackTrace()
}