# Usa la imagen oficial de ngrok como base
FROM ngrok/ngrok

# Crea el directorio de trabajo y cámbiate a él.
# El usuario "ngrok" por defecto tiene permisos aquí.
WORKDIR /home/ngrok

# Crea el subdirectorio de configuración.
# La instrucción anterior ya te ha movido a un lugar con permisos.
RUN mkdir -p .ngrok2

# CMD para ejecutar la autenticación y el túnel
# CMD ["/bin/sh", "-c", "ngrok authtoken \"$(cat /run/secrets/ngrok_authtoken)\" && ngrok http jenkins:8080"]
# CMD ngrok authtoken `cat /run/secrets/ngrok_authtoken` && ngrok http jenkins:8080
CMD http jenkins:8080