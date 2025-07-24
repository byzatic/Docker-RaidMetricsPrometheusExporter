package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.RAIDMetricsCollectorInterface;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.Scheduler.JobDetail;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.Scheduler.Scheduler;

import java.io.IOException;
import java.net.URL;

public class RAIDMetricsService implements RAIDMetricsServiceInterface {

    private final static Logger logger = LoggerFactory.getLogger(RAIDMetricsService.class);
    private final Integer port;
    private final String cronExpressionString;
    private final RAIDMetricsCollectorInterface collector;
    private final String address;
    private final String location;
    private Integer state = 0;

    public RAIDMetricsService(URL prometheusEndpointURL, String cronExpressionString, RAIDMetricsCollectorInterface collector) {
        this.port = prometheusEndpointURL.getPort();
        this.address = prometheusEndpointURL.getHost();
        this.location = prometheusEndpointURL.getPath();
        //
        this.cronExpressionString = cronExpressionString;
        //
        this.collector = collector;
    }

    @Override
    public void run() throws IOException {
        try (HTTPServer server = HTTPServer.builder().hostname(address).port(port).buildAndStart()) {
            logger.debug("HTTPServer listening on port http://{}:{}{}", address,  port, "/metrics");
            logger.warn("HTTPServer using default location {}", "/metrics");

            Process updateMetricsProcess = new Process(collector);
            Scheduler scheduler = new Scheduler();
            JobDetail jobDetail = new JobDetail(updateMetricsProcess, cronExpressionString);
            scheduler.addTask(jobDetail);
            do {
                scheduler.runAllTasks(true);
                if (updateMetricsProcess.healthStatus != 0) {
                    throw new RuntimeException("Update metrics process finished with error");
                }
            } while (state == 0);
        }
    }
    @Override
    public void terminate() {
        state = 1;
    }

    private static class Process implements Runnable{
        private final RAIDMetricsCollectorInterface collector;

        public Integer healthStatus = 0;

        public Process(RAIDMetricsCollectorInterface collector) {
            this.collector = collector;
        }

        @Override
        public void run() {
            try {
                this.collector.updateMetrics();
                healthStatus = 0;
            } catch (Exception e) {
                healthStatus = 1;
                logger.error("Error updating metrics", e);
                // Останавливаем выполнение задачи
                throw new RuntimeException("Stopping scheduled task due to exception in updateMetrics", e);
            }
        }
    }
}
