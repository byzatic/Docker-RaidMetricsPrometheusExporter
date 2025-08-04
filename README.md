# Docker Raid Metrics Prometheus Exporter

## Description

Docker Raid Metrics Prometheus Exporter is a Java-based application designed to collect and expose RAID metrics for Prometheus using `smartmontools` ([smartmontools.org](https://www.smartmontools.org/)) for LSI MegaRAID and similar controllers. Running within a Docker container, it provides monitoring and alerting capabilities for RAID disk health and status.

Core Features:
- RAID Disk Health Monitoring via smartctl
- Prometheus-Compatible Metrics
- Customizable Scheduling via cron expressions
- Configurable disk device list
- Lightweight Dockerized deployment

---

## Running with Docker

This project provides simple scripts and a ready-to-use `docker-compose.yml` for launching the exporter.

### Prerequisites

- Docker
- Prometheus scraping `http://<host>:20222/metrics`

### Usage

#### Start the container

```bash
./docker.up.sh
```

#### View logs

```bash
./docker.logs.sh
```

#### Stop the container

```bash
./docker.down.sh
```

### docker-compose.yml

```yaml
version: '3.7'
services:
  raidmetrics-exporter:
    image: byzatic/docker-raidmetrics-prometheus-exporter:latest
    container_name: raidmetrics-exporter
    privileged: true
    ports:
      - "20222:8080"
    volumes:
      - ${PWD}/logs:/app/logs
    logging:
      driver: json-file
      options:
        max-file: "10"
        max-size: "10m"
    networks:
      - raidmetrics-net

networks:
  raidmetrics-net:
    name: raidmetrics-net
    external: false
```

---

## Application Parameters

```xml
<Configuration>
    <cronExpressionString>*/5 * * * * ?</cronExpressionString>
    <prometheusEndpointURL>http://0.0.0.0:8080/metrics</prometheusEndpointURL>
</Configuration>
```

- **cronExpressionString**: Defines scheduling interval. Default: `*/5 * * * * ?`
- **prometheusEndpointURL**: Prometheus-compatible HTTP endpoint. Default: `http://0.0.0.0:8080/metrics`

---

## Exported Metrics (smartctl-based)

Each metric includes labels:
- `disk_id`
- `model`
- `serial`
- `device_name`

### Example Metrics

```text
# HELP megaraid_current_pending_sectors Current pending sectors
# TYPE megaraid_current_pending_sectors gauge
megaraid_current_pending_sectors{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 0.0
megaraid_current_pending_sectors{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 0.0
megaraid_current_pending_sectors{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 1.0

# HELP megaraid_offline_uncorrectable Offline uncorrectable sectors
# TYPE megaraid_offline_uncorrectable gauge
megaraid_offline_uncorrectable{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 0.0
megaraid_offline_uncorrectable{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 0.0
megaraid_offline_uncorrectable{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 0.0

# HELP megaraid_power_on_hours Power on hours per disk
# TYPE megaraid_power_on_hours gauge
megaraid_power_on_hours{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 2329.0
megaraid_power_on_hours{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 26098.0
megaraid_power_on_hours{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 49707.0

# HELP megaraid_reallocated_sectors Reallocated sectors count per disk
# TYPE megaraid_reallocated_sectors gauge
megaraid_reallocated_sectors{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 0.0
megaraid_reallocated_sectors{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 0.0
megaraid_reallocated_sectors{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 0.0

# HELP megaraid_smart_passed SMART overall health passed status (1=PASSED, 0=FAILED)
# TYPE megaraid_smart_passed gauge
megaraid_smart_passed{device_name="/dev/bus/1",disk_id="14",model="WDC WD4005FFBX-68CAUN0",serial="WD-BS01LX5H"} 1.0
megaraid_smart_passed{device_name="/dev/bus/1",disk_id="15",model="WDC WD20EFZX-68AWUN0",serial="WD-WX62D71C524S"} 1.0
megaraid_smart_passed{device_name="/dev/bus/1",disk_id="17",model="HGST HUS722T2TALA604",serial="WMC6N0L5DK62"} 1.0

# HELP megaraid_temperature_celsius Disk temperature in Celsius
# TYPE megaraid_temperature_celsius gauge
megaraid_temperature_celsius{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 28.0
megaraid_temperature_celsius{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 27.0
megaraid_temperature_celsius{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 30.0

# HELP megaraid_udma_crc_errors UDMA CRC error count
# TYPE megaraid_udma_crc_errors gauge
megaraid_udma_crc_errors{disk_id="14",model="WDC WD4005FFBX-68CAUN0",mount_point="/dev/bus/1",serial="WD-BS01LX5H"} 0.0
megaraid_udma_crc_errors{disk_id="15",model="WDC WD20EFZX-68AWUN0",mount_point="/dev/bus/1",serial="WD-WX62D71C524S"} 0.0
megaraid_udma_crc_errors{disk_id="17",model="HGST HUS722T2TALA604",mount_point="/dev/bus/1",serial="WMC6N0L5DK62"} 0.0
```

These metrics allow real-time health inspection of disks behind MegaRAID with minimal overhead.