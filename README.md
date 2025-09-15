# 🚀 Automatización de CI/CD con Jenkins & Ngrok

Este proyecto automatiza la Integración y Despliegue Continuos (CI/CD) de tus aplicaciones. Utiliza Jenkins para orquestar pipelines que se activan automáticamente mediante webhooks de GitHub. Ngrok expone el servidor de Jenkins de forma segura, permitiendo la comunicación entre GitHub y tu entorno local sin necesidad de una IP pública.

## ✨ Características clave

* **Jenkins en Docker:** Despliega tu servidor de automatización de manera rápida y consistente.
* **Webhooks de GitHub:** Automatiza la ejecución de pipelines con cada `push` al repositorio.
* **Exposición segura:** Usa Ngrok para conectar tu entorno local a los servicios de GitHub.
* **Notificaciones de Slack:** Recibe actualizaciones en tiempo real sobre el estado de tus pipelines.
* **Ejecución condicional:** Reconstruye la aplicación solo cuando se detectan cambios en archivos importantes.

## 🛠️ Tecnologías utilizadas

* **Docker & Docker Compose:** Contenedores para un entorno de desarrollo y producción reproducible.
* **Jenkins:** Servidor de automatización líder para la orquestación de pipelines.
* **Ngrok:** Servicio de túneles inversos para exponer servicios locales de forma segura a Internet.
* **GitHub Webhooks:** Mecanismo de notificación para la integración entre repositorios y Jenkins.
* **Slack:** Plataforma de comunicación para notificaciones del flujo de CI/CD.

## ⚙️ Requisitos previos

Asegúrate de tener instalados los siguientes componentes y credenciales:

* **Docker** y **Docker Compose**
* Una cuenta de **GitHub** y un **token de acceso personal** (PAT)
* Una cuenta de **Ngrok** y un **authtoken**
* Un **workspace de Slack** y permisos para crear una integración

---

## 🚀 Guía de inicio rápido

Sigue estos pasos para desplegar y configurar tu entorno de CI/CD.

### Paso 1: Clonar el repositorio y configurar el entorno

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/johnnynaranjo/CI-CD-Automation-with-Jenkins---Ngrok.git
    cd CI-CD-Automation-with-Jenkins---Ngrok
    ```
2.  Renombra `.env.example` a `.env` y edita los valores con tu configuración:
    ```bash
    # .env
    GITHUB_USERNAME=tu_usuario_de_github
    JENKINS_PORT=8081
    AGENT_PORT=50000
    ```
3.  Crea la carpeta `secrets` y los archivos con los tokens. Asegúrate de que los nombres de los archivos coincidan con los IDs que usarás en Jenkins (por ejemplo, `github-token`, `ngrok-authtoken`, `slack-token`).
    ```graphql
    (directorio principal)
    └── secrets/
        ├── github-token
        ├── ngrok-authtoken
        └── slack-token
    ```
    Añade los tokens correspondientes en cada archivo.

### Paso 2: Desplegar los servicios con Docker

Ejecuta el siguiente comando para iniciar Jenkins y Ngrok en segundo plano:
```bash
docker-compose up -d
```
Una vez que los contenedores estén activos, puedes acceder a Jenkins:
   - **Localmente**: en `http://localhost:8081`.
   - **Públicamente**: Ngrok creará una URL de túnel que puedes encontrar en los logs del contenedor de Ngrok o en tu dashboard de la interfaz web.

### Paso 3: Configurar Jenkins, GitHub y Slack

#### Configuración de la integración con Slack
1. En tu workspace de Slack, selecciona con el botón derecho el canal donde vas a recibir las notificaciones y accede a `Ver información del canal`.
2. En la pestaña de `Integraciones` haz clic en `Añadir una aplicacion`.
3. Busca la aplicación `Jenkins` y haz clic en `Instalar`.
4. Se abrirá una ventana del navegador, haz clic en `Add to Slack`.
5. Selecciona el canal donde vas recibir las notificaciones y haz clic en `Add Jenkins CI integration`
6. Guarda la informacion que aparece para insertarla en `Jenkins`

#### Configuración de Slack en Jenkins
1. Dentro de Jenkins, ve a `Manage Jenkins` > `System` y busca la sección de **Slack**.
2. En `Workspace` introduce la informacion del `Team Subdomain` de la configuracion de `Slack`
3. En `Credential` selecciona el `slack_token` enviado por `Docker Secrets`
4. En `Default channel / member id` introduce el canal de `Slack` donde vas a recibir las notificaciones

#### Configuración del webhook de GitHub
1. En tu repositorio de GitHub, navega a `Settings` > `Webhooks` y haz clic en `Add webhook`.
2. En Payload URL, usa la URL pública de Ngrok seguida de `/github-webhook/`.
   - Ejemplo: `https://tu-url-ngrok.ngrok-free.app/github-webhook/`
3. Selecciona `application/json` como Content type.
4. Deja el resto de las configuraciones por defecto y haz clic en Add webhook.

#### Creación y configuración de la pipeline
1. En Jenkins, crea un **Nuevo Ítem** (`New Item`) y selecciona Pipeline.
2. En la configuración, marca la opción **GitHub hook trigger for GITScm polling** en la sección **Build Triggers**.
3. En la sección `Pipeline`, selecciona `Pipeline script` y configura tu script.
   - **parameters**: ajusta los parametros del proyecto 

¡Listo! Con cada `push` a tu repositorio, la pipeline de Jenkins se activará automáticamente, construyendo y desplegando tu aplicación.