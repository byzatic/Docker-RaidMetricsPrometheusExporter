package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service;

import java.io.IOException;

public interface RAIDMetricsServiceInterface {
    void run() throws IOException;

    void terminate();
}
