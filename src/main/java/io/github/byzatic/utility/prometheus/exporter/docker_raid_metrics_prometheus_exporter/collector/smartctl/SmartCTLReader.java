package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.model.MegaRAIDDiskInfo;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.CollectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SmartCTLReader {
    private final static Logger logger = LoggerFactory.getLogger(SmartCTLReader.class);
    private final List<String> deviceNames;

    public SmartCTLReader(List<String> deviceNames) {
        this.deviceNames = deviceNames;
    }

    public List<MegaRAIDDiskInfo> readDisks() throws CollectorException {
        List<MegaRAIDDiskInfo> disks = new ArrayList<>();

        for (String dev : deviceNames) {
            for (int i = 0; i < 32; i++) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("smartctl", "-a", "-d", "megaraid," + i, "/dev/" + dev);
                    Process process = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    MegaRAIDDiskInfo disk = new MegaRAIDDiskInfo();
                    disk.diskId = i;
                    disk.mountPoint = "<not mounted>";

                    boolean found = false;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Device Model")) {
                            disk.model = line.split(":", 2)[1].trim();
                            found = true;
                        } else if (line.contains("Serial Number")) {
                            disk.serial = line.split(":", 2)[1].trim();
                        } else if (line.contains("SMART overall-health")) {
                            disk.smartStatus = line.split(":", 2)[1].trim();
                        } else if (line.contains("Reallocated_Sector_Ct")) {
                            disk.reallocatedSectors = extractSmartValue(line);
                        } else if (line.contains("Power_On_Hours")) {
                            disk.powerOnHours = extractSmartValue(line);
                        } else if (line.contains("Temperature_Celsius")) {
                            disk.temperatureCelsius = extractSmartValue(line);
                        } else if (line.contains("Current_Pending_Sector")) {
                            disk.currentPendingSectors = extractSmartValue(line);
                        } else if (line.contains("Offline_Uncorrectable")) {
                            disk.offlineUncorrectable = extractSmartValue(line);
                        } else if (line.contains("UDMA_CRC_Error_Count")) {
                            disk.udmaCrcErrors = extractSmartValue(line);
                        }
                    }

                    if (found) {
                        disks.add(disk);
                    }

                    process.waitFor();
                } catch (Exception e) {
                    logger.debug("Disk {} on device /dev/{} not accessible or not present.", i, dev);
                }
            }
        }

        return disks;
    }

    private int extractSmartValue(String line) {
        String[] parts = line.trim().split("\\s+");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            return -1;
        }
    }
}