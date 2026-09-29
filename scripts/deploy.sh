#!/usr/bin/env bash
set -euo pipefail
set +x
: "${TOMCAT_URL:?Set TOMCAT_URL}"
: "${TOMCAT_USER:?Set TOMCAT_USER}"
: "${TOMCAT_PASSWORD:?Supply the Jenkins credential}"
war=${1:-target/cicd-demo.war}
test -s "$war" || { echo 'WAR is missing or empty' >&2; exit 1; }
umask 077
auth=$(mktemp)
response=$(mktemp)
trap 'rm -f "$auth" "$response"' EXIT
# Keep credentials out of command arguments and console output. Generated secrets are hex.
[[ "$TOMCAT_USER" =~ ^[a-zA-Z0-9_-]+$ && "$TOMCAT_PASSWORD" =~ ^[a-zA-Z0-9_-]+$ ]] || {
    echo 'Deployment credentials must use letters, digits, underscores or hyphens.' >&2; exit 1;
}
printf 'user = "%s:%s"\n' "$TOMCAT_USER" "$TOMCAT_PASSWORD" > "$auth"
curl --silent --show-error --fail --connect-timeout 10 --max-time 120 \
    --config "$auth" --upload-file "$war" \
    "$TOMCAT_URL/manager/text/deploy?path=/cicd-demo&update=true" > "$response"
if ! head -n 1 "$response" | grep -q '^OK -'; then
    echo 'Tomcat rejected the deployment:' >&2
    cat "$response" >&2
    exit 1
fi
cat "$response"
