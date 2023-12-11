#!/bin/bash

set -euo pipefail

REPO_PATH=${REPO_PATH:-/repo}
REPO_URL=${REPO_URL:-https://github.com/CVEProject/cvelistV5.git}
SERVICE_ENDPOINT=${SERVICE_ENDPOINT:-http://osv-nvd-service:8080/cves}

if [ -f files_to_sync ]; then
    last_line=`tail -1 files_to_sync`
    echo "[" > cves
    for f in `cat files_to_sync`; do
        cve=${f##*/}
        if [ $f == $last_line ]; then
            echo "  \"${cve/.json/}\"" >> cves
        else
            echo "  \"${cve/.json/}\"," >> cves
        fi
    done
    echo "]" >> cves
    curl -H "Content-Type: application/json" ${SERVICE_ENDPOINT} -d @cves
    rm cves files_to_sync
fi