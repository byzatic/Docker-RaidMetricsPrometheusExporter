#!/usr/bin/env bash

#set -e
#set -u
#set -o pipefail

EXTERNAL_ARGS=${*}

env

#
echo "[INFO] external args> ${*}"
if ! sh -c "java ${*} -jar /app/app.jar"; then exit 1; fi
#
