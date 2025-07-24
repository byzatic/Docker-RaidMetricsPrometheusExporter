package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions;

import java.io.IOException;

public class MegaCLICollectorException extends Exception {
    public MegaCLICollectorException(String message) {
        super(message);
    }

    public MegaCLICollectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MegaCLICollectorException(Exception e) {
        super(e);
    }
}
