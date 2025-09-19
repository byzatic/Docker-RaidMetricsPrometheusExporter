package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector;

import org.jetbrains.annotations.NotNull;

public interface SmartctlCollectorFactoryInterface {
    @NotNull RAIDMetricsCollectorInterface getCollector(@NotNull Boolean caching);
}
