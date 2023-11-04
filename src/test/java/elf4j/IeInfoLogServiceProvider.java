package elf4j;

import elf4j.spi.LogServiceProvider;
import elf4j.util.IeLogger;

public class IeInfoLogServiceProvider implements LogServiceProvider {

    @Override
    public Logger logger() {
        return IeLogger.INFO;
    }
}
