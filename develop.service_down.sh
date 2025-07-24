#!/usr/bin/env bash
#
#
#

docker-compose -f ./docker-compose.yml down --remove-orphans --volumes --rmi all develop-docker-raid-metrics-prometheus-exporter


