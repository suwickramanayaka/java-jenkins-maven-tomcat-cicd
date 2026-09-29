#!/usr/bin/env bash
set -euo pipefail
password=$(cat /run/secrets/tomcat_password)
[[ "$password" =~ ^[a-f0-9]{48}$ ]] || { echo 'Invalid generated Tomcat secret' >&2; exit 1; }
umask 077
cat > "$CATALINA_HOME/conf/tomcat-users.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<tomcat-users>
  <role rolename="manager-script"/>
  <user username="deployer" password="$password" roles="manager-script"/>
</tomcat-users>
EOF
mkdir -p "$CATALINA_HOME/webapps/manager" "$CATALINA_HOME/webapps/ROOT"
cp -a /opt/manager/. "$CATALINA_HOME/webapps/manager/"
printf '%s\n' 'Tomcat is ready. Application: /cicd-demo/' > "$CATALINA_HOME/webapps/ROOT/index.html"
exec catalina.sh run
