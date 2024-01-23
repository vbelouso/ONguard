#!/bin/bash

set -euo pipefail

source ${BASH_SOURCE%/*}/common.sh

if [ ! -d ${REPO_PATH} ]; then
  echo Cloning $REPO_URL into $REPO_PATH
  git clone --depth=3 ${REPO_URL} ${REPO_PATH}
else
  echo "Skip clone. Directory exists"
fi