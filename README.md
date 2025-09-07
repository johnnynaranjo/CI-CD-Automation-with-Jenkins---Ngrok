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
      git clone https://github.com/tu-usuario/nombre-del-repo.git
      cd nombre-del-repo
      ```
   
   2. Renombra `.env.example` a `.env` y configura tus valores:

      ```bash
      # .env
      # Variables de entorno para Jenkins
      GITHUB_USERNAME=tu_usuario_de_github
      GITHUB_TOKEN_ID=tu_token_de_github

      # Variables de entorno para Ngrok
      NGROK_AUTHTOKEN=tu_token_de_ngrok
      JENKINS_SUBDOMAIN=tu_subdominio_personalizado_ngrok # Opcional, si tienes una cuenta premium

      # Puertos
      JENKINS_PORT=8080
      AGENT_PORT=50000
      ```

   3. Ejecuta los servicios con Docker Compose:

      ```bash
      docker-compose up -d
      ```
      
      Esto iniciará los contenedores de Jenkins y Ngrok en segundo plano.

   4. Accede a Jenkins:

      - Localmente: Jenkins estará disponible en http://localhost:8081.

      - Públicamente: Ngrok creará una URL pública que podrás ver en los logs del contenedor de Ngrok o en la interfaz web de Ngrok en http://localhost:4040. Utiliza esta URL para configurar el webhook en tu repositorio de GitHub.

## 🔗 Configuración del Webhook en GitHub

   1. En tu repositorio de GitHub, ve a `Settings` > `Webhooks`.

   2. Haz clic en ```Add webhook```.

   3. En `Payload URL`, introduce la URL pública de Ngrok seguida de `/github-webhook/`. Por ejemplo: `https://tu-url-ngrok.ngrok-free.app/github-webhook/`.

   4. En Content type, selecciona `application/json`.

   5. Deja el resto de las configuraciones por defecto y haz clic en Add webhook.

## 💻 Creación de la Pipeline en Jenkins

   1. Inicia sesión en Jenkins.

   2. Ve a `New Item` y selecciona `Pipeline`.

   3. En la configuración, marca la opción `GitHub hook trigger for GITScm polling`.

   4. En la sección `Pipeline`, selecciona `Pipeline script from SCM`.

      - **SCM**: `Git`

      - **Repository URL**: La URL de tu repositorio de GitHub.

      - **Credentials**: Añade tus credenciales de GitHub (token personal).

      - **Script Path**: `Jenkinsfile` (asumiendo que tu pipeline se define en un archivo llamado así en la raíz de tu repo).

Ahora, cada push a tu repositorio activará la pipeline de Jenkins automáticamente.