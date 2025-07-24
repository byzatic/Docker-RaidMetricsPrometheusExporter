package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.MegaCLICollectorException;

public interface MegaCLIContentReaderInterface {
    String readContent() throws MegaCLICollectorException;
}
