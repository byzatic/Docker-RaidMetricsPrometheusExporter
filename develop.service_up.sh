#!/usr/bin/env bash
#
#
#

docker-compose -f ./docker-compose.yml up --detach --force-recreate --remove-orphans --build develop-docker-raid-metrics-prometheus-exporter
