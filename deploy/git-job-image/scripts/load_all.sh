#!/bin/bash

set -euo pipefail

source ${BASH_SOURCE%/*}/common.sh

PROGRESS_FILE=${WORKDIR}/progress
MAX_ELEMENTS=50
WINDOW_TIME=30

if [ -f ${PROGRESS_FILE} ]; then
  milestone=$(tail -n 1 "${PROGRESS_FILE}")
fi

function progress_reached_current_folder() {
  if [ -z ${milestone:-} ]; then
    return 0
  fi
  if [ $milestone == $1 ]; then
    milestone=""
    echo "Resume load at ${1}"
    return 0
  fi
  return 1
}

for yd in ${REPO_PATH}/cves/*/ ; do
  if [ -d "$yd" ]; then
    year=$(basename ${yd})
    echo "Loading directories in ${year}"
    for d in $yd/*/ ; do
      if [ -d "$d" ]; then
        echo "Loading CVEs in $year/$(basename ${d})"
        current_folder=$year/$(basename ${d})
        batch_count=0
        for f in $d/*.json ; do
          if [ -f "$f" ]; then
            if [ "${#buffer[@]}" -eq $MAX_ELEMENTS ]; then
              current_progress="${current_folder}:${batch_count}"
              if progress_reached_current_folder $current_progress ; then
                send_buffer
              fi
              buffer=()
              echo "${current_progress}" > ${PROGRESS_FILE}
              ((batch_count++))
            fi
            filename="$(basename ${f})"
            buffer+=(\"${filename/.json/}\")
          fi
        done
        if [ "${#buffer[@]}" -gt 0 ]; then
          send_buffer
        fi
      fi
    done
  fi
done
