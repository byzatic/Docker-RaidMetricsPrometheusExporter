package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.SmartCTLReader;
import org.jetbrains.annotations.NotNull;

public class SmartctlCollectorFactory implements SmartctlCollectorFactoryInterface {
    private final SmartCTLReader smartCTLReader;

    public SmartctlCollectorFactory(@NotNull SmartCTLReader smartCTLReader) {
        this.smartCTLReader = smartCTLReader;
    }

    @Override
    public @NotNull RAIDMetricsCollectorInterface getCollector(@NotNull Boolean caching) {
        if (caching) {
            return new RAIDMetricsCollectorWithCaching(smartCTLReader);
        } else {
            return new RAIDMetricsCollector(smartCTLReader);
        }
    }
}
