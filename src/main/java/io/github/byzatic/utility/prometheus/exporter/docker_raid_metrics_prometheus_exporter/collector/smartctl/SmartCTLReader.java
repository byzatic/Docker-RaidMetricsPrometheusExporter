package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl;

import com.google.gson.Gson;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.dto.SmartctlDevice;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.smartctl.dto.SmartctlScanResult;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.model.MegaRAIDDiskInfo;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.CollectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SmartCTLReader {
    private final static Logger logger = LoggerFactory.getLogger(SmartCTLReader.class);

    public List<MegaRAIDDiskInfo> readDisks() throws CollectorException {
        List<MegaRAIDDiskInfo> disks = new ArrayList<>();
        List<DeviceEntry> allDevices = scanDevices();

        boolean hasMegaRAID = allDevices.stream().anyMatch(dev -> dev.driver.startsWith("megaraid"));
        logger.debug("hasMegaRAID is {}", hasMegaRAID);

        for (DeviceEntry device : allDevices) {
            logger.debug("process device: {}", device);
            if (hasMegaRAID && !device.driver.startsWith("megaraid")) {
                logger.debug("Dkip regular device");
                continue; // если есть megaraid-диски — игнорируем обычные
            }

            try {
                List<String> cmd = new ArrayList<>();
                cmd.add("smartctl");
                cmd.add("-a");

                if (device.driver.startsWith("megaraid")) {
                    cmd.add("-d");
                    cmd.add(device.driver);
                }
                cmd.add(device.dev);

                ProcessBuilder pb = new ProcessBuilder(cmd);
                logger.debug("try to run {}", pb.command());
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                MegaRAIDDiskInfo disk = new MegaRAIDDiskInfo();
                disk.diskId = extractMegaRAIDIndex(device.driver);
                disk.deviceName = device.dev;

                boolean found = false;
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.trace("Line parser; line is {}", line);
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
                    logger.debug("Device {} processed correctly", device);
                    disks.add(disk);
                } else {
                    logger.debug("Device {} is not processed correctly", device);
                }

                process.waitFor();
            } catch (Exception e) {
                logger.debug("Device {} not accessible or not present.", device.dev);
            }
        }

        return disks;
    }

    private List<DeviceEntry> scanDevices() throws CollectorException {
        logger.debug("Starts scan devices");
        List<DeviceEntry> devices = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("smartctl", "--scan", "-j").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Чтение всего JSON в строку
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            process.waitFor();

            String json = jsonBuilder.toString();
            Gson gson = new Gson();

            SmartctlScanResult result = gson.fromJson(json, SmartctlScanResult.class);

            // Валидация версии JSON
            if (result.json_format_version == null || result.json_format_version.size() != 2 ||
                    result.json_format_version.get(0) != 1 || result.json_format_version.get(1) != 0) {
                throw new CollectorException("Unsupported or missing json_format_version: " + result.json_format_version);
            }

            // Валидация устройств
            if (result.devices == null || result.devices.isEmpty()) {
                throw new CollectorException("No devices found in smartctl JSON output");
            }

            for (SmartctlDevice device : result.devices) {
                if (device.name == null || device.type == null) {
                    throw new CollectorException("Invalid device entry: " + device);
                }
                devices.add(new DeviceEntry(device.name, device.type));
            }

        } catch (Exception e) {
            throw new CollectorException("Failed to parse smartctl --scan -j output", e);
        }

        logger.debug("Scan devices result: {}", devices);
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

        @Override
        public String toString() {
            return "DeviceEntry{" +
                    "dev='" + dev + '\'' +
                    ", driver='" + driver + '\'' +
                    '}';
        }
    }
}