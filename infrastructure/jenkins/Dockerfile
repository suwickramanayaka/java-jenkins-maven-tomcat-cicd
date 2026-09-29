FROM jenkins/jenkins:2.568.3-jdk21
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --latest=false --plugin-file /usr/share/jenkins/ref/plugins.txt
RUN mkdir -p /var/jenkins_home/agent-bootstrap
COPY init.groovy /usr/share/jenkins/ref/init.groovy.d/01-project.groovy.override
COPY known_hosts /etc/ssh/ssh_known_hosts
