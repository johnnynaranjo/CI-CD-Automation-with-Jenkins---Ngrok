FROM jenkins/jenkins:lts

USER root

# Instala Docker CLI y Compose
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y docker-ce-cli docker-compose-plugin && \
    apt-get clean

# Crea el grupo docker si no existe y añade el usuario jenkins
# Ejecuta grep ```docker /etc/group``` en el host para ver el GID del grupo docker
# En mi caso es 989, así que lo creo con ese GID
RUN groupadd -g 989 docker && usermod -aG docker jenkins

# Copia plugins y script de inicialización
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
COPY pipeline_slack.groovy /usr/share/jenkins/ref/jobs/pipeline_slack.groovy

# Instala plugins automáticamente en primer arranque
RUN jenkins-plugin-cli --plugins -f /usr/share/jenkins/ref/plugins.txt

# Vuelve al usuario jenkins
USER jenkins
