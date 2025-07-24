package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.prometheus.metrics.core.metrics.Gauge;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli.MegaCLIContentReaderInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MegaRAIDMetricsCollector implements MegaRAIDMetricsCollectorInterface {
    private final static Logger logger = LoggerFactory.getLogger(MegaRAIDMetricsCollector.class);

    private final MegaCLIContentReaderInterface executor;

    // Метрики
    private static final Gauge diskStatus = Gauge.builder()
            .name("megaraid_disk_status")
            .help("Status of each disk (1 for OK, 0 for Failure)")
            .labelNames("disk_id")
            .register();

    private static final Gauge diskTemperature = Gauge.builder()
            .name("megaraid_disk_temperature")
            .help("Temperature of each disk in Celsius")
            .labelNames("disk_id")
            .register();

    private static final Gauge smartAlert = Gauge.builder()
            .name("megaraid_smart_alert")
            .help("SMART alert flag for each disk (1 if alert is flagged, 0 otherwise)")
            .labelNames("disk_id")
            .register();

    public MegaRAIDMetricsCollector(MegaCLIContentReaderInterface executor) {
        this.executor = executor;
    }

    // Обновление метрик
    @Override
    public void updateMetrics() {
        try {
            parseAndUpdateMetrics(executor.readContent());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Парсинг данных и обновление метрик
    private void parseAndUpdateMetrics(String output) {
        // Регулярные выражения для парсинга состояния диска, температуры и предупреждения SMART
        Pattern diskStatusPattern = Pattern.compile("Device Id: (\\d+).*?Firmware state: (\\w+)", Pattern.DOTALL);
        Pattern temperaturePattern = Pattern.compile("Device Id: (\\d+).*?Drive Temperature\\s*:(\\d+)C", Pattern.DOTALL);
        Pattern smartAlertPattern = Pattern.compile("Device Id: (\\d+).*?Drive has flagged a S\\.M\\.A\\.R\\.T alert\\s*:\\s*(Yes|No)", Pattern.DOTALL);

        // Обновление статуса диска
        Matcher statusMatcher = diskStatusPattern.matcher(output);
        while (statusMatcher.find()) {
            String diskId = statusMatcher.group(1);
            String state = statusMatcher.group(2);
            diskStatus.labelValues(diskId).set("Online".equals(state) ? 1 : 0);
        }

        // Обновление температуры диска
        Matcher tempMatcher = temperaturePattern.matcher(output);
        while (tempMatcher.find()) {
            String diskId = tempMatcher.group(1);
            double temp = Double.parseDouble(tempMatcher.group(2));
            diskTemperature.labelValues(diskId).set(temp);
        }

        // Обновление метрики SMART
        Matcher smartMatcher = smartAlertPattern.matcher(output);
        while (smartMatcher.find()) {
            String diskId = smartMatcher.group(1);
            String alertStatus = smartMatcher.group(2);
            smartAlert.labelValues(diskId).set("Yes".equals(alertStatus) ? 1 : 0);
        }
    }
}
