package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl;

import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.model.MegaRAIDDiskInfo;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.CollectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartCTLReader {
    private final static Logger logger = LoggerFactory.getLogger(SmartCTLReader.class);

    public List<MegaRAIDDiskInfo> readDisks() throws CollectorException {
        List<MegaRAIDDiskInfo> disks = new ArrayList<>();
        List<DeviceEntry> allDevices = scanDevices();

        boolean hasMegaRAID = allDevices.stream().anyMatch(dev -> dev.driver.startsWith("megaraid"));

        for (DeviceEntry device : allDevices) {
            if (hasMegaRAID && !device.driver.startsWith("megaraid")) {
                continue; // если есть megaraid-диски — игнорируем обычные
            }

            try {
                ProcessBuilder pb = new ProcessBuilder("smartctl", "-a", "-d", device.driver, device.dev);
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                MegaRAIDDiskInfo disk = new MegaRAIDDiskInfo();
                disk.diskId = extractMegaRAIDIndex(device.driver);
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
                logger.debug("Device {} not accessible or not present.", device.dev);
            }
        }

        return disks;
    }

    private List<DeviceEntry> scanDevices() throws CollectorException {
        List<DeviceEntry> devices = new ArrayList<>();
        Pattern scanPattern = Pattern.compile("(?<dev>/dev/\\S+)\\s+-d\\s+(?<driver>\\S+)");

        try {
            Process process = new ProcessBuilder("smartctl", "--scan").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = scanPattern.matcher(line);
                    if (matcher.find()) {
                        devices.add(new DeviceEntry(matcher.group("dev"), matcher.group("driver")));
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            throw new CollectorException("Failed to execute smartctl --scan", e);
        }

        return devices;
    }

    private int extractSmartValue(String line) {
        String[] parts = line.trim().split("\\s+");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            return -1;
        }
    }

    private int extractMegaRAIDIndex(String driver) {
        if (driver.startsWith("megaraid,")) {
            try {
                return Integer.parseInt(driver.substring("megaraid,".length()));
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static class DeviceEntry {
        String dev;
        String driver;

        DeviceEntry(String dev, String driver) {
            this.dev = dev;
            this.driver = driver;
        }
    }
}