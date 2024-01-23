#!/bin/bash

set -euo pipefail

source ${BASH_SOURCE%/*}/common.sh

WINDOW_TIME=0

if [ -f "${WORKDIR}/files_to_sync" ]; then
  for f in `cat ${WORKDIR}/files_to_sync`; do
    cve=${f##*/}
    buffer+=(\"${cve/.json/}\")
  done
  send_buffer
fi