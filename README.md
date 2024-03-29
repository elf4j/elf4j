[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

# elf4j - Easy Logging Facade for Java

API and SPI of a no-fluff Java logging facade

* ... because sometimes the wheel should be reinvented.

## User stories

1. As a Java application developer, I want to use a log service API, so that I can choose or switch to use any compliant log service provider, at application deployment time without code change or
   re-compile.
2. As a log service/engine/framework provider, I want to implement a Service Provider
   Interface [(SPI)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), so that the log service client can
   opt to use my service implementation, at application deployment time without code change or re-compile.

## Prerequisite

Java 8 or better, although individual logging service providers may have higher JDK version prerequisite.

## What it is...

### A logging Service Interface and Access API

```java
public interface Logger {
    static Logger instance() {
        return LogServiceProviderLocator.INSTANCE.logServiceProvider().logger();
    }

    default Logger atTrace() {
        return this.atLevel(Level.TRACE);
    }

    default Logger atDebug() {
        return this.atLevel(Level.DEBUG);
    }

    default Logger atInfo() {
        return this.atLevel(Level.INFO);
    }

    default Logger atWarn() {
        return this.atLevel(Level.WARN);
    }

    default Logger atError() {
        return this.atLevel(Level.ERROR);
    }

    Logger atLevel(Level level);

    Level getLevel();

    boolean isEnabled();

    void log(Object message);

    void log(String message, Object... arguments);

    void log(Throwable throwable);

    void log(Throwable throwable, Object message);

    void log(Throwable throwable, String message, Object... arguments);
}
```

### A logging Service Provider Interface (SPI)

```java
public interface LogServiceProvider {
    Logger logger();
}
```

### Conventions, Defaults, and Implementation Notes (a.k.a. "the spec")

#### Thread safety

Any `Logger` instance should be thread-safe.

#### Severity Level

If a Logger instance is obtained via the `Logger.instance()` static factory method, then the default severity level of
such instance is decided by the _service provider_ implementation. If a Logger instance is obtained via one of
the `Logger.at<Level>` instance factory methods, then its severity level should be as requested.

#### Placeholder token

The empty curly braces token `{}` should be the placeholder for message arguments. This is by convention, and does not
syntactically appear in the API or SPI. Both the API user and the Service Provider must honor such convention.

#### Lazy arguments

Lazy arguments are those whose runtime type is `java.util.function.Supplier`. Compared to other types of arguments, lazy
ones have to be treated specially in that the `Supplier` function must be applied first before the result is used as the
substitution to the argument placeholder. This special handling of lazy arguments is by convention, and not
syntactically enforced by the API or SPI. It allows for the API user to mix up lazy and eager arguments within the same
logging method call.

## Get it

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

Install as a compile-scope dependency in Maven or other build tools alike.

## Use it - for a client of the logging service API

```java
class SampleUsage {
    static Logger logger = Logger.instance();

    @Nested
    class plainText {
        @Test
        void declarationsAndLevels() {
            logger.log(
                    "Logger instance is thread-safe so it can be declared and used as a local, instance, or static variable");
            logger.log("Default severity level is decided by the logging provider implementation");
            Logger trace = logger.atTrace();
            trace.log("Explicit severity level is specified by user i.e. TRACE");
            Logger.instance().atTrace().log("Same explicit level TRACE");
            logger.atDebug().log("Severity level is DEBUG");
            logger.atInfo().log("Severity level is INFO");
            trace.atWarn().log("Severity level is WARN, not TRACE");
            logger.atError().log("Severity level is ERROR");
            Logger.instance()
                    .atDebug()
                    .atError()
                    .atTrace()
                    .atWarn()
                    .atInfo()
                    .log("Not a practical example but the severity level is INFO");
        }
    }

    @Nested
    class textWithArguments {
        Logger info = logger.atInfo();

        @Test
        void lazyAndEagerArgumentsCanBeMixed() {
            info.log("Message can have any number of arguments of {} type", Object.class.getTypeName());
            info.log(
                    "Lazy arguments, of {} type, whose values may be {} can be mixed with eager arguments of non-Supplier types",
                    Supplier.class.getTypeName(),
                    (Supplier) () -> "expensive to compute");
            info.atWarn()
                    .log("The Supplier downcast is mandatory per lambda syntax because arguments are declared as generic Object rather than functional interface");
        }
    }

    @Nested
    class throwable {
        @Test
        void asTheFirstArgument() {
            Exception exception = new Exception("Exception message");
            logger.atError().log(exception);
            logger.atError().log(exception, "Optional log message");
            logger.atInfo()
                    .log(exception,
                            "Exception is always the first argument to a logging method. The {} log message and following arguments work the same way {}.",
                            "optional",
                            (Supplier) () -> "as usual");
        }
    }
}
```

Note that elf4j is a logging service facade and specification, rather than the implementation. As such,

### No-op by default

- Nothing will be logging out (no-op) unless a properly configured [elf4j service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-elf4j) JAR is
  discovered at the application start time. The elf4j facade itself only ships with a default no-op logging provider.

### Only one in-effect logging provider

- The elf4j API user can select or change into using
  any [elf4j service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-elf4j) at deploy
  time, without application code change or re-compile.
- The recommended setup is to ensure that only one desired logging provider with its associated JAR(s) be present in the
  classpath; or, no provider JAR when no-op is desired. In this case, nothing further is needed for elf4j
  to work.
- If multiple eligible providers are present in classpath, somehow, then the system property `elf4j.service.provider.fqcn` has to be
  used to select the desired provider. No-op applies if the specified provider is absent.

  ```
  java -Delf4j.service.provider.fqcn="elf4j.log4j.Log4jLoggerFactory" MyApplication
  ```

  With the default no-op logging provider, this system property can also be used to turn OFF all logging services
  discovered by the elf4j facade:

  ```
  java -Delf4j.service.provider.fqcn="elf4j.util.NoopLogServiceProvider" MyApplication
  ```

- It is considered a setup error to have multiple providers in the classpath without a selection. The elf4j facade falls
  back to no-op on any setup errors.

## Use it - for a _service provider_ implementing the logging service SPI

To enable an independent logging framework/engine via the elf4j spec, the _service provider_ should follow instructions
of Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html). Namely,
the implementation should include

- the _provider class_ implementing
  the `LogServiceProvider` [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), the _service class_
  implementing the `Logger` API, and their associated classes as needed
- the _provider-configuration_ file, named `elf4j.spi.LogServiceProvider` in the resource directory `META-INF/services`,
  whose content is the Fully Qualified Name of the SPI _provider class_ implementing the `LogServiceProvider` SPI
  interface

## Available logging _service providers_ of elf4j

- A native elf4j service provider: [elf4j-provider](https://github.com/elf4j/elf4j-provider)
- [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
- [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
- [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
- [Java 4 (java.util.logging) provider](https://github.com/elf4j/elf4j-jul)
- [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)
- ...

![Visitor Count](https://profile-counter.glitch.me/elf4j/count.svg)
