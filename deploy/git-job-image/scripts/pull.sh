#!/bin/bash

set -euo pipefail

source ${BASH_SOURCE%/*}/common.sh

if [ ! -d ${REPO_PATH} ]; then
    ${BASH_SOURCE%/*}/clone.sh
    ${BASH_SOURCE%/*}/load_all.sh
else
    echo Pulling latest data from ${REPO_URL}
    LATEST_COMMIT=`git -C ${REPO_PATH} rev-parse HEAD`
    git -C ${REPO_PATH} pull origin main
    git -C ${REPO_PATH} diff --name-only HEAD ${LATEST_COMMIT} | grep CVE > ${WORKDIR}/files_to_sync
    ${BASH_SOURCE%/*}/sync.sh
    rm ${WORKDIR}/files_to_sync
fi
