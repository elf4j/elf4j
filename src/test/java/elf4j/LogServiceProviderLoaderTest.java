package elf4j;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import elf4j.spi.LogServiceProvider;
import elf4j.util.NoopLogServiceProvider;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogServiceProviderLoaderTest {

    @Nested
    class notSetup {

        @Test
        void whenNoLogServiceProviderLoadedAndNotSetup() {
            LogServiceProviderLocator.LogServiceProviderLoader logServiceProviderLoader =
                    new LogServiceProviderLocator.LogServiceProviderLoader(null, false);

            assertTrue(logServiceProviderLoader.getLogServiceProvider() instanceof NoopLogServiceProvider);
        }
    }

    @Nested
    class Setup {
        @Mock
        ServiceLoader<LogServiceProvider> mockLogServiceProviderServiceLoader;

        @Mock
        Iterator<LogServiceProvider> mockLogServiceProviderIterator;

        @Mock
        LogServiceProvider mockLogServiceProvider;

        @Test
        void whenLogServiceProviderLoadedAndSetup() {
            LogServiceProviderLocator.LogServiceProviderLoader logServiceProviderLoader =
                    new LogServiceProviderLocator.LogServiceProviderLoader(mockLogServiceProviderServiceLoader, true);
            given(mockLogServiceProviderServiceLoader.iterator()).willReturn(mockLogServiceProviderIterator);
            given(mockLogServiceProviderIterator.next()).willReturn(mockLogServiceProvider);

            LogServiceProvider logServiceProvider = logServiceProviderLoader.getLogServiceProvider();

            then(mockLogServiceProviderIterator).should().next();
            assertSame(mockLogServiceProvider, logServiceProvider);
        }
    }
}
