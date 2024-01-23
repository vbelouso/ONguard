#!/bin/bash

set -euo pipefail

WORKDIR=${WORKDIR:-./work}
REPO_PATH=${REPO_PATH:-./repo}/cvelistV5
REPO_URL=${REPO_URL:-https://github.com/CVEProject/cvelistV5.git}
SERVICE_ENDPOINT=${SERVICE_ENDPOINT:-http://onguard:8080}

declare -a buffer=()

function send_buffer() {
  printf -v cves '%s,' "${buffer[@]}"
  data="[${cves%,}]"
  echo "Loading CVEs: ${data[@]}"
  curl --silent --output /dev/null -H "Content-Type: application/json" ${SERVICE_ENDPOINT}/vulnerabilities?reload=true\&omitBody=true -d "${data}"
  sleep $WINDOW_TIME
  buffer=()
}