package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.UtilLogger;

public class TestLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger() {
        return UtilLogger.INFO;
    }
}
