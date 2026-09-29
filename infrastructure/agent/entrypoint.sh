#!/usr/bin/env bash
set -euo pipefail
for attempt in $(seq 1 120); do
    if test -s /run/agent-bootstrap/secret && curl -fsS --connect-timeout 3 --max-time 15 \
        http://jenkins:8080/jnlpJars/agent.jar -o /tmp/agent.jar; then
        exec java -Xmx256m -jar /tmp/agent.jar -url http://jenkins:8080/ \
            -secret @/run/agent-bootstrap/secret -name maven-agent -webSocket \
            -workDir /home/jenkins/agent
    fi
    sleep 2
done
echo 'Jenkins agent bootstrap timed out' >&2
exit 1
