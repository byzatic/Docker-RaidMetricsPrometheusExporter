package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Configuration {

    // TODO: а потом это вынести в Singleton

    private final static Logger logger = LoggerFactory.getLogger(App.class);
    public static final String APP_NAME = "docker-raid-metrics-prometheus-exporter";

    public static final String APP_VERSION = "1.0";

    //  The cronExpressionString is a string that defines the schedule for periodic tasks using the cron format.
    //  It typically consists of six fields separated by spaces:
    //    1. Seconds (0–59)
    //	  2. Minutes (0–59)
    //	  3. Hours (0–23)
    //	  4. Day of Month (1–31)
    //	  5. Month (1–12 or JAN–DEC)
    //	  6. Day of Week (0–7 or SUN–SAT, where both 0 and 7 represent Sunday)
    // Each field can contain specific values, ranges, lists, or wildcards to set the schedule precisely. For example:
    //    - "0 0/5 * * * ?" runs every 5 minutes.
    //    - "0 15 10 * * ?" runs at 10:15 AM every day.
    //    - "0 0 12 1/5 * ?" runs every fifth day at noon.
    // In Java libraries like Quartz, the cron expression is used to specify when the task should execute, with each
    // part interpreted in the context of time units from seconds to days of the week.

    public static final String CRON_EXPRESSION_STRING;
    public static final URL PROMETHEUS_URL;
    public static final List<String> BLOCK_DEVICES;

    static {
        try {
            logger.debug("Static block is executed.");

            Configurations configs = new Configurations();

            XMLConfiguration config = configs.xml("configuration/configuration.xml");

            CRON_EXPRESSION_STRING = config.getString("cronExpressionString");
            PROMETHEUS_URL = new URI(config.getString("prometheusEndpointURL")).toURL();
            String blockDevicesString = config.getString("blockDevices");
            if (blockDevicesString == null || blockDevicesString.isEmpty()) throw new RuntimeException("Empty blockDevices");
            BLOCK_DEVICES = List.of(blockDevicesString.split("\\s*,\\s*"));
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error("Exception : " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error reading URL", e);
        } catch (ConfigurationException ce) {
            logger.error("Exception : " + ExceptionUtils.getStackTrace(ce));
            throw new RuntimeException("Error reading configuration", ce);
        }
    }
}
