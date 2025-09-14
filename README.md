# 🚀 CI/CD Automation with Jenkins & Ngrok

   Este proyecto te permite desplegar un entorno de Integración Continua y Despliegue Continuo (CI/CD) completamente automatizado. Utiliza Jenkins para la orquestación de pipelines y Ngrok para exponer de forma segura el host de Jenkins a Internet, permitiendo la integración de webhooks de GitHub.

   Cada vez que se realiza un push o una modificación en un repositorio de GitHub, Jenkins detectará el cambio y activará una pipeline predefinida para reconstruir y desplegar tu aplicación.

   Este proyecto define una infraestructura mínima de CI/CD con:

   - Jenkins corriendo en Docker
   - Acceso remoto via Ngrok (útil para entornos sin IP pública)
   - Reconstrucción automática vía GitHub Webhooks
   - Ejecución condicional de builds

## 🛠️ Tecnologías Utilizadas

   - Docker & Docker Compose: Contenedorización de servicios para un despliegue consistente y portátil.

   - Jenkins: Servidor de automatización open-source para la orquestación de pipelines de CI/CD.

   - Ngrok: Herramienta para exponer un servicio local a Internet de forma segura, facilitando la conexión con los webhooks de GitHub.

   - GitHub Webhooks: Mecanismo de notificación que permite a Jenkins recibir eventos del repositorio.

## ⚙️ Requisitos Previos

   Antes de comenzar, asegúrate de tener instalados los siguientes componentes:

   - Docker y Docker Compose

   - Una cuenta de GitHub y un token de acceso personal (PAT)

   - Una cuenta de Ngrok y tu token de autenticación

## 🚀 Despliegue del Entorno

   1. Clona este repositorio:

      ```bash
      git clone https://github.com/johnnynaranjo/CI-CD-Automation-with-Jenkins---Ngrok.git
      cd CI-CD-Automation-with-Jenkins---Ngrok
      ```

   2. Renombra `.env.example` a `.env` y configura tus valores:
   
      ```bash
      # .env
      # Renombrar el archivo a .env
      # Github credentials
      GITHUB_USERNAME=tu_usuario_de_github

      # Puertos expuestos en el host
      JENKINS_PORT=8080
      AGENT_PORT=50000
      ```

   3. Crea la carpeta secrets con los archivos siguientes e introduce en ellos los tokens correspondientes:
      - `github_token`
      - `ngrok_authtoken`
      - `slack_token`  

   4. Ejecuta los servicios con Docker Compose:
      ```bash
      docker-compose up -d
      ```
      Esto iniciará los contenedores de Jenkins y Ngrok en segundo plano.

   5. Accede a Jenkins:

      - Localmente: Jenkins estará disponible en http://localhost:8081.

      - Públicamente: Ngrok creará una URL pública que podrás ver en los logs del contenedor de Ngrok o en la interfaz web de Ngrok en http://localhost:4040. Utiliza esta URL para configurar el webhook en tu repositorio de GitHub.

   6. Configura los parametros de la pipeline:
      - `GITHUB_REPO_URL`
      - `SLACK_CHANNEL`

## 🔗 Configuración del Webhook en GitHub

   1. En tu repositorio de GitHub, ve a `Settings` > `Webhooks`.

   2. Haz clic en ```Add webhook```.

   3. En `Payload URL`, introduce la URL pública de Ngrok seguida de `/github-webhook/`. Por ejemplo: `https://tu-url-ngrok.ngrok-free.app/github-webhook/`.

   4. En Content type, selecciona `application/json`.

   5. Deja el resto de las configuraciones por defecto y haz clic en Add webhook.

## 🔗 Configuración del canal en Slack

   1. En tu espacio de trabajo de Slack, selecciona con el botón derecho el canal donde vas a recibir las notificaciones y accede a `Ver información del canal`.

   2. En la pestaña de `Integraciones` haz clic en `Añadir una aplicacion`.

   3. Busca la aplicación `Jenkins` y haz clic en `Instalar`.

   4. Se abrirá una ventana del navegador, haz clic en `Add to Slack`.

   5. Selecciona el canal donde vas recibir las notificaciones y haz clic en `Add Jenkins CI integration`

   6. Guarda la informacion que aparece para insertarla en `Jenkins`

## 🔗 Configuración de Slack en Jenkins

   1. Inicia sesión en Jenkins.

   2. Ve a `Manage Jenkins`> `System` > `Slack`.

   3. En `Workspace`introduce la informacion del `Team Subdomain` de la configuracion de `Slack`

   4. En `Credential` selecciona el `slack_token` enviado por `Docker Secrets`

   5. En `Default channel / member id`introduce el canal de `Slack` donde vas a recibir las notificaciones.

## 💻 Creación de la Pipeline en Jenkins

   1. Inicia sesión en Jenkins.

   2. Ve a `New Item` y selecciona `Pipeline`.

   3. En la configuración, marca la opción `GitHub hook trigger for GITScm polling` en la seccion de `Triggers`.

   4. En la sección `Pipeline`, selecciona `Pipeline script from SCM`.

      - **SCM**: `Git`

      - **Repository URL**: La URL de tu repositorio de GitHub.

      - **Credentials**: Añade tus credenciales de GitHub (token personal).

      - **Script Path**: `Jenkinsfile` (asumiendo que tu pipeline se define en un archivo llamado así en la raíz de tu repo).

Ahora, cada push a tu repositorio activará la pipeline de Jenkins automáticamente.