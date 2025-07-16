package elf4j;

import elf4j.spi.LoggerFactory;

public class TestLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger() {
        return TestLogger.INFO;
    }
}
