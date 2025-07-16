package elf4j;

import static org.junit.jupiter.api.Assertions.*;

import elf4j.util.NoopLoggerFactory;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryLoaderTest {
    @Nullable String clearedLoggerFactorySpiFqcn = System.clearProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN);

    @BeforeEach
    void beforeEach() {
        clearedLoggerFactorySpiFqcn = System.clearProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN);
    }

    @Test
    void givenUnknownSpiFqcnSpecifiedAndTestProvider_thenNop() {
        System.setProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN, "testUnknownSpiImplClassName");

        assertInstanceOf(NoopLoggerFactory.class, LoggerFactoryLocator.INSTANCE.getLoggerFactory());

        System.clearProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN);
    }

    @Test
    void givenNoSpiFqcnSpecifiedAndTestProvider_thenTestProvider() {
        assert clearedLoggerFactorySpiFqcn == null;

        assertInstanceOf(TestLoggerFactory.class, LoggerFactoryLocator.INSTANCE.getLoggerFactory());
    }

    @AfterEach
    void afterEach() {
        if (clearedLoggerFactorySpiFqcn != null) {
            System.setProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN, clearedLoggerFactorySpiFqcn);
        }
    }
}
