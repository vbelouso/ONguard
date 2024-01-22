#!/bin/bash

set -euo pipefail

REPO_PATH=${REPO_PATH:-/repo/cves}
REPO_URL=${REPO_URL:-https://github.com/CVEProject/cvelistV5.git}

if [ ! -d ${REPO_PATH} ]; then
    ${BASH_SOURCE%/*}/clone.sh
else
    echo Pulling latest data from ${REPO_URL}
    LATEST_COMMIT=`git -C ${REPO_PATH} rev-parse HEAD`
    git -C ${REPO_PATH} pull origin main
    git -C ${REPO_PATH} diff --name-only HEAD ${LATEST_COMMIT} | grep CVE > files_to_sync
fi
