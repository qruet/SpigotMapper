package dev.qruet.mapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for writing messages to the designated log file
 *
 * @author Qruet
 */
public class Logger {

    private final File logFile;

    public Logger(File file) {
        if (!file.exists()) {
            throw new UnsupportedOperationException("Passed log file reference does not exist.");
        }
        this.logFile = file;
    }

    /**
     * This function is responsible for writing
     * data into the log file
     *
     * @param line String message
     * @return True if {@param line} was written to the log file successfully
     */
    public boolean writeLine(String line) {
        try {
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(line + "\n");
            writer.close(); // cleanup
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
