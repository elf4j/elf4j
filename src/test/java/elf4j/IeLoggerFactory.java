package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.IeLogger;

public class IeLoggerFactory implements LoggerFactory {

    @Override
    public Logger logger() {
        return IeLogger.INFO;
    }
}
