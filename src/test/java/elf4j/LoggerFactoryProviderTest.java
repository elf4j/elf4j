package elf4j;

import elf4j.util.NoopLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class LoggerFactoryProviderTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Nested
    class fallback {
        @Test
        void noProvider() {
            assertSame(NoopLogger.INSTANCE, Logger.instance());
        }

        @Test
        void selectedProviderNotFound() {
            System.setProperty(LoggerFactoryProvider.ELF4J_LOGGER_FACTORY_FQCN, "NonExistingProviderFactoryFqcn");

            assertSame(NoopLogger.INSTANCE, Logger.instance());
        }
    }
}