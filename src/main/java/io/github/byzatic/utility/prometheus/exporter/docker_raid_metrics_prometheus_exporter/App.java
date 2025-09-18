package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.RAIDMetricsCollector;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.RAIDMetricsCollectorInterface;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.SmartCTLReader;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.RAIDMetricsService;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.RAIDMetricsServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.debug("MegaRAID metrics service is running...");

        RAIDMetricsCollectorInterface collector = new RAIDMetricsCollector(new SmartCTLReader());
        RAIDMetricsServiceInterface megaRAIDMetricsService = new RAIDMetricsService(Configuration.PROMETHEUS_URL, Configuration.CRON_EXPRESSION_STRING, collector);

        try {
            megaRAIDMetricsService.run();
        } catch (Exception e) {
            megaRAIDMetricsService.terminate();
            logger.error("An error was occurred: {}", e.getMessage());
            logger.debug("Stacktrace: ", e);
            throw new RuntimeException(e);
        }

    }
}
