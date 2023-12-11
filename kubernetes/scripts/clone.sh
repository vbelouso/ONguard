#!/bin/bash

set -euo pipefail

REPO_PATH=${REPO_PATH:-/repo}/cvelistV5
REPO_URL=${REPO_URL:-https://github.com/CVEProject/cvelistV5.git}

if [ ! -d ${REPO_PATH} ]; then
    echo Cloning $REPO_URL into $REPO_PATH
    git clone --depth=3 ${REPO_URL} ${REPO_PATH}
else
    echo "Skip clone. Directory exists"
fi