# Docker Raid Metrics Prometheus Exporter

## Description

Docker Raid Metrics Prometheus Exporter is a Java-based application designed to collect and expose RAID metrics for Prometheus from LSI MegaRAID controllers. Running within a Docker container, it provides monitoring and alerting capabilities for RAID status, health, and performance.

Core Features:
- RAID Health Monitoring: Collects health data from LSI MegaRAID controllers, including status of arrays, disks, and controllers.
- Prometheus-Compatible Metrics: Exposes metrics in a format compatible with Prometheus, enabling easy integration into monitoring and alerting systems.
- Customizable Scheduling: Uses cron expressions to control how frequently metrics are collected.
- Flexible Configuration: Allows file paths, scheduling, and Prometheus endpoint settings to be customized via environment variables or system properties.

## Documentation description for each application parameter

Application Parameters
1.	cronExpressionString
- Purpose: Defines the scheduling interval for periodic tasks within the application.
- Type: String
- Default: "*/5 * * * * ?" (Runs every 5 seconds)
- Example: To set this to run every 10 minutes, specify -DcronExpressionString="0 0/10 * * * ?".
- Usage: System.getProperty("cronExpressionString")
2. megacliContentFilePath
- Purpose: Specifies the file path where MegaCLI output content is stored. This file is typically used to read or write logs from MegaCLI, a command-line utility for managing LSI MegaRAID controllers.
- Type: Path
- Default: "data/megacli.log"
- Example: To use a different file path, specify -DmegacliContentFilePath="/path/to/your/file.log".
- Usage: System.getProperty("megacliContentFilePath")
3.	megacliLockFilePath
- Purpose: Specifies the file path for the MegaCLI lock file, which ensures exclusive access when reading or writing MegaCLI data.
- Type: Path
- Default: "data/megacli.log"
- Example: To set a different lock file, specify -DmegacliLockFilePath="/path/to/lock/file.log".
- Usage: System.getProperty("megacliLockFilePath")
4. prometheusEndpointURL
- Purpose: Sets the URL endpoint for Prometheus to access application metrics.
- Type: URL
- Default: http://0.0.0.0:8080/metrics
- Example: To set a different Prometheus endpoint, specify -DprometheusEndpointURL="http://localhost:9090/metrics".
- Usage: System.getProperty("prometheusEndpointURL")


## LSI MegaRAID Disk Health Metrics

This set of Prometheus metrics provides insights into the health and status of disks managed by LSI MegaRAID. Each metric is labeled with the disk identifier (`disk_id`), allowing monitoring of specific disks.

### Metrics

#### 1. `megaraid_disk_status`
- **Description**: Indicates the health status of each disk.
- **Values**:
    - `1` for disks that are functioning correctly (OK).
    - `0` for disks that have encountered a failure.
- **Example**:
    - `megaraid_disk_status{disk_id="15"} 1.0` shows that disk 15 is operating normally.
    - `megaraid_disk_status{disk_id="17"} 1.0` shows that disk 17 is also operating normally.

#### 2. `megaraid_disk_temperature`
- **Description**: Provides the current temperature of each disk in Celsius.
- **Example**:
    - `megaraid_disk_temperature{disk_id="15"} 20.0` indicates that disk 15 is at 20°C.
    - `megaraid_disk_temperature{disk_id="17"} 24.0` indicates that disk 17 is at 24°C.

#### 3. `megaraid_smart_alert`
- **Description**: Represents the SMART (Self-Monitoring, Analysis, and Reporting Technology) alert status for each disk.
- **Values**:
    - `1` if an alert is flagged, indicating potential issues.
    - `0` if no alert is flagged, suggesting that no problems have been detected.
- **Example**:
    - `megaraid_smart_alert{disk_id="15"} 0.0` shows that no alert is flagged for disk 15.
    - `megaraid_smart_alert{disk_id="17"} 0.0` shows that no alert is flagged for disk 17.

### Metric Type

All these metrics are defined as **`gauge`** types, which are suitable for values that can vary over time. They provide essential health and diagnostic data for each disk in the array, useful for monitoring, alerting, and proactive disk management.