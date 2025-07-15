package elf4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryLoaderTest {

    @Nested
    class notSetup {

        @Test
        void givenNoSpecifiedAndTestProvider() {
            String targetLoggerFactory = System.clearProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN);
            //            System.setProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN,
            // NoopLoggerFactory.class.getName());
            assertInstanceOf(
                    TestLoggerFactory.class,
                    LoggerFactoryLocator.INSTANCE.getLoggerFactory(),
                    "Assuming test logger factory is set up to be loaded");
            if (targetLoggerFactory != null) {
                System.setProperty(LoggerFactoryLocator.ELF4J_SERVICE_PROVIDER_FQCN, targetLoggerFactory);
            }
        }
    }
}
