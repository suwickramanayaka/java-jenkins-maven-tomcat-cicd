#!/usr/bin/env bash
set -euo pipefail
base=${1:?Usage: smoke-test.sh BASE_URL EXPECTED_COMMIT}
expected=${2:?Provide the exact expected Git commit}
for attempt in $(seq 1 30); do
    health=$(curl -fsS --connect-timeout 3 --max-time 5 "$base/health" 2>/dev/null || true)
    actual=$(curl -fsS --connect-timeout 3 --max-time 5 "$base/version" 2>/dev/null || true)
    if [[ "$health" == UP && "$actual" == "$expected" ]]; then
        page=$(curl -fsS --connect-timeout 3 --max-time 5 "$base/")
        if [[ "$page" == *'Current release'* && "$page" == *"${expected:0:12}"* ]]; then
            echo "Verified healthy application and release $actual at $base/"
            exit 0
        fi
    fi
    sleep 2
done
echo "Deployment verification failed: expected release $expected at $base" >&2
exit 1
