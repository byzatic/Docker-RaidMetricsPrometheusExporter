package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.MegaCLICollectorException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class MegaCLIFileContentReader implements MegaCLIContentReaderInterface {
    private static final Logger logger = LoggerFactory.getLogger(MegaCLIFileContentReader.class);
    private final Path filePath;
    private final Path lockFilePath;

    public MegaCLIFileContentReader(Path filePath, Path lockFilePath) {
        this.filePath = filePath;
        this.lockFilePath = lockFilePath;
    }

    private String readFileContent() throws IOException, MegaCLICollectorException {
        logger.debug("Reading file content: {}", filePath);

        if (lockFilePath != null && Files.exists(lockFilePath)) {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path lockDir = lockFilePath.getParent();
                if (lockDir == null) {
                    throw new MegaCLICollectorException("Lock file directory is invalid.");
                }

                lockDir.register(watchService, StandardWatchEventKinds.ENTRY_DELETE);

                // Проверяем наличие lockFile и ожидаем его удаление до 100 мс
                long startTime = System.currentTimeMillis();
                while (Files.exists(lockFilePath)) {
                    WatchKey key = watchService.poll(100 - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE &&
                                    lockFilePath.endsWith(event.context().toString())) {
                                break; // Блокировка снята, выходим из цикла
                            }
                        }
                        key.reset();
                    }

                    if (System.currentTimeMillis() - startTime >= 100) {
                        throw new MegaCLICollectorException("Waiting for lock release exceeded 100 ms.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MegaCLICollectorException("Thread interrupted while waiting for lock file to be released.", e);
            }
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        logger.debug("Reading file content complete: {}", content);
        return content.toString();
    }

    @Override
    public String readContent() throws MegaCLICollectorException {
        try {
            return readFileContent();
        } catch (IOException | MegaCLICollectorException e) {
            throw new MegaCLICollectorException(e);
        }
    }
}