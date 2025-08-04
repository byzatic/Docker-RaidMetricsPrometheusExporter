package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.dto;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.dto.SmartctlDevice;

import java.util.List;

public class SmartctlScanResult {
    public List<Integer> json_format_version;
    public List<SmartctlDevice> devices;
}
