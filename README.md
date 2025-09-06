# Jenkins + Docker + Ngrok CI/CD Setup

Este proyecto define una infraestructura mínima de CI/CD con:

- Jenkins corriendo en Docker
- Acceso remoto via Ngrok (útil para entornos sin IP pública)
- Reconstrucción automática vía GitHub Webhooks
- Ejecución condicional de builds

---

## 🚀 Servicios

### 🔧 Jenkins

- Corre en el puerto `8081` (configurable vía `.env`)
- Usa el Docker del host (`/var/run/docker.sock`)
- Datos persistidos en `jenkins_home/`

### 🌐 Ngrok

- Exposición temporal de Jenkins a través de túneles seguros
- Configuración mediante `ngrok.yml` y variables de entorno

---

## 📁 Archivos Clave

| Archivo              | Descripción                                         |
|----------------------|-----------------------------------------------------|
| `docker-compose.yml` | Define Jenkins y Ngrok como servicios               |
| `.env.example`       | Plantilla de configuración                          |
| `.gitignore`         | Archivos y carpetas que deben excluirse del repo    |
| `Jenkinsfile`        | Pipeline declarativa para despliegues automáticos   |
| `ngrok.yml.example`  | Plantilla de configuración Ngrok                    |

---

## 📦 Instrucciones Rápidas

1. Renombra `.env.example` a `.env` y configura tus valores.
2. Renombra `ngrok.yml.example` a `ngrok.yml` y configura tus valores.
3. Construye los servicios:

   ```bash
   docker compose up -d --build
   ```