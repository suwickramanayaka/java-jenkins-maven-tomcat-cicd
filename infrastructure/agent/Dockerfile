FROM maven:3.9.11-eclipse-temurin-21
USER root
RUN apt-get update && apt-get install -y --no-install-recommends git curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && (getent passwd 1000 >/dev/null && userdel -r $(getent passwd 1000 | cut -d: -f1) || true) \
    && groupadd -f -g 1000 jenkins \
    && useradd -m -u 1000 -g 1000 -d /home/jenkins jenkins \
    && mkdir -p /home/jenkins/agent /home/jenkins/.m2 \
    && chown -R 1000:1000 /home/jenkins
ENV HOME=/home/jenkins MAVEN_CONFIG=/home/jenkins/.m2
COPY --chmod=755 entrypoint.sh /usr/local/bin/project-agent
COPY known_hosts /etc/ssh/ssh_known_hosts
USER 1000:1000
WORKDIR /home/jenkins/agent
ENTRYPOINT ["/usr/local/bin/project-agent"]
