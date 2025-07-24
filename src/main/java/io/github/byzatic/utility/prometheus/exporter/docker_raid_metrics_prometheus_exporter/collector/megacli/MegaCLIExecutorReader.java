package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.megacli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.collector.exceptions.MegaCLICollectorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MegaCLIExecutorReader implements MegaCLIContentReaderInterface {
    private final static Logger logger = LoggerFactory.getLogger(MegaCLIExecutorReader.class);


    @Override
    public String readContent() throws MegaCLICollectorException {
        try {
            return executeCommand("megacli -PDList -aALL");
        } catch (Exception e) {
            throw new MegaCLICollectorException(e);
        }
    }

    public String executeCommand(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        logger.debug("Created ProcessBuilder for command: bash -c {} ", command);

        Process process = processBuilder.start();
        logger.debug("process started");


        int errFlag = 0;
        BufferedReader errorBufferedErrorReader = new BufferedReader(
                new InputStreamReader(
                        process.getErrorStream()
                )
        );
        String errorMessage = errorBufferedErrorReader.readLine();
        while (errorMessage != null) {
            logger.error("Execution error message: {}", errorMessage);
            errorMessage = errorBufferedErrorReader.readLine();
            if (errFlag != 1) {
                errFlag = 1;
            }
        }
        errorBufferedErrorReader.close();

        BufferedReader outputBufferedErrorReader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()
                )
        );
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = outputBufferedErrorReader.readLine()) != null) {
            output.append(line).append("\n");
            logger.debug("Process returns: {}", output);
        }
        outputBufferedErrorReader.close();

        if (errFlag != 0) {
            String errMessage = "An error has occurred";
            logger.error(errMessage);
            throw new RuntimeException(errMessage);
        } else {
            logger.debug("process complete");
        }

        return output.toString();
    }
}
