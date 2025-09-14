# Usa la imagen oficial de ngrok como base
FROM ngrok/ngrok:latest

# Directorio de trabajo con permisos para el usuario ngrok
WORKDIR /home/ngrok

# Prepara el directorio de configuración (aunque no es estrictamente necesario)
RUN mkdir -p .ngrok2

# ENTRYPOINT para registrar el token desde el secret
# y luego lanzar el túnel como proceso principal
ENTRYPOINT sh -c "ngrok authtoken $(cat /run/secrets/ngrok_authtoken) && exec ngrok http jenkins:8080"