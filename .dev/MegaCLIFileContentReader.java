package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.MegaCLICollectorException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

public class MegaCLIFileContentReader implements MegaCLIContentReaderInterface {
    private static final Logger logger = LoggerFactory.getLogger(MegaCLIFileContentReader.class);
    private final Path filePath;
    private final Path lockFilePath;
    private final Duration timeoutDuration;

    public MegaCLIFileContentReader(Path filePath, Path lockFilePath, Duration timeoutDuration) {
        this.filePath = filePath;
        this.lockFilePath = lockFilePath;
        this.timeoutDuration = timeoutDuration;
    }

    private String readFileContent() throws InterruptedException, MegaCLICollectorException {
        logger.debug("Чтение содержимого файла: {}", filePath);
        StringBuilder content = new StringBuilder();


        long startTime = System.currentTimeMillis();
        while (true) {
            try (FileChannel lockChannel = FileChannel.open(filePath, StandardOpenOption.READ);
                 FileLock lock = lockChannel.tryLock(0, Long.MAX_VALUE, true)) {

                // Файл доступен для чтения
                logger.debug("Чтение файла...");
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                logger.debug("Чтение содержимого файла завершено: {}", content);
                break; // Выходим из цикла после успешного чтения

            } catch (Exception e) {
                logger.debug("Файл заблокирован для записи, ожидание...");

                // Проверяем, не превышено ли время ожидания
                if (System.currentTimeMillis() - startTime > timeoutDuration.toMillis()) {
                    logger.error("Превышено время ожидания доступности файла для чтения.");
                    throw new MegaCLICollectorException("Превышено время ожидания доступности файла для чтения.");
                }

                try {
                    Thread.sleep(100); // Задержка перед повторной попыткой
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Ожидание было прервано.");
                    throw new MegaCLICollectorException("Ожидание было прервано.");
                }
            }
        }
        return content.toString();
    }

    @Override
    public String readContent() throws MegaCLICollectorException {
        try {
            return readFileContent();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MegaCLICollectorException("Прервано во время ожидания блокировки файла.", e);
        } catch (Exception e) {
            throw new MegaCLICollectorException(e);
        }
    }
}