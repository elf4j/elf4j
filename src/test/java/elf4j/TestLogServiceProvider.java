package elf4j;

import elf4j.spi.LogServiceProvider;

public class TestLogServiceProvider implements LogServiceProvider {

    @Override
    public Logger logger() {
        return TestLogger.INFO;
    }
}
