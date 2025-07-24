package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.MegaRAIDMetricsCollector;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.MegaRAIDMetricsCollectorInterface;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli.MegaCLIExecutorReader;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.MegaRAIDMetricsService;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.MegaRAIDMetricsServiceInterface;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {
        logger.debug("MegaRAID metrics service is running...");

//        MegaRAIDMetricsCollectorInterface collector = new MegaRAIDMetricsCollector(new MegaCLIFileContentReader(Configuration.MEGACLI_CONTENT_FILE_PATH, Configuration.MEGACLI_LOCK_FILE_PATH));
        MegaRAIDMetricsCollectorInterface collector = new MegaRAIDMetricsCollector(new MegaCLIExecutorReader());
        MegaRAIDMetricsServiceInterface megaRAIDMetricsService = new MegaRAIDMetricsService(Configuration.PROMETHEUS_URL, Configuration.CRON_EXPRESSION_STRING, collector);

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
