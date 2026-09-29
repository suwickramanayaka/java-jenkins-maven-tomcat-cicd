FROM tomcat:10.1.60-jdk21-temurin
RUN mkdir -p /opt/manager && cp -a /usr/local/tomcat/webapps.dist/manager/. /opt/manager/ \
    && groupadd -g 10001 app && useradd -u 10001 -g app -s /usr/sbin/nologin app \
    && chown -R app:app /usr/local/tomcat
COPY manager-context.xml /opt/manager/META-INF/context.xml
COPY --chmod=755 entrypoint.sh /usr/local/bin/project-tomcat
USER app
ENTRYPOINT ["/usr/local/bin/project-tomcat"]
