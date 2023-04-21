#!/bin/bash

set -eo pipefail

DATA_DIR=/var/lib/clickhouse/

if [ -s "$DATA_DIR/data" ]; then
  echo "Database already initialized. Removing initdb files."
  rm -v /docker-entrypoint-initdb.d/*
fi

/entrypoint.sh