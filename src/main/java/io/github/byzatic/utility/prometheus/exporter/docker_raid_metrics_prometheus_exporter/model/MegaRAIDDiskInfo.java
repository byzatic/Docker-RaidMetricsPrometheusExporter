package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.model;

public class MegaRAIDDiskInfo {
    public int diskId;
    public String model;
    public String serial;
    public String deviceName;
    public String smartStatus;
    public int reallocatedSectors;
    public int powerOnHours;
    public int temperatureCelsius;
    public int currentPendingSectors;
    public int offlineUncorrectable;
    public int udmaCrcErrors;
}
