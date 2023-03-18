# elf4j

API and SPI of a no-fluff Java logging facade - Easy Logging Facade for Java (ELF4J)

## User Stories

1. As an application developer, I want to use a logging service API, so that I can choose or change the actual logging
   engine framework among various service providers, at the application deployment time without code change.
2. As a logging service provider, I want to have a Service Provider
   Interface [(SPI)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) to implement, so that my independent
   logging framework can be chosen and used by the client application at its deployment time without code change.

* The basic delivery mechanism for the user stories is intended to be the
  Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).

## Prerequisite

Java 8 or better

## Get It...

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

## Use it...

### Service Interface and Access API

```java
public interface Logger {
    static Logger instance() {
        return ServiceProviderLocator.INSTANCE.loggerFactory().logger();
    }

    Logger atTrace();

    Logger atDebug();

    Logger atInfo();

    Logger atWarn();

    Logger atError();

    Level getLevel();

    boolean isEnabled();

    void log(Object message);

    void log(String message, Object... args);

    void log(Throwable t);

    void log(Throwable t, Object message);

    void log(Throwable t, String message, Object... args);
}
```

### Service Provider Interface (SPI)

```java
public interface LoggerFactory {
    Logger logger();
}
```

### Conventions, Defaults, and Implementation Notes (a.k.a."the Spec")

#### Placeholder Token

The empty curly braces token `{}` should be the placeholder for message arguments. This is by convention, and does
not syntactically appear in the API or SPI. Both the API user and the Service Provider must honor such convention.

#### Thread Safety

Any `Logger` instance should be thread-safe.

#### Severity Level

If a Logger instance is obtained via the `Logger.instance()` method, then the default severity level of such instance is
decided by the Service Provider implementation. If a Logger instance is obtained via one of the fluent-style `at<Level>`
methods, then its severity level should be as requested.

#### Lazy Arguments

An `Object` type argument passed to any of the logging methods must be treated specially if the actual type at
runtime is `java.util.function.Supplier`. That is, the Supplier function must be applied first before the function
result is used to compute the final log message.

The special handling of lazy arguments is by convention, and not syntactically enforced by the API or SPI. This allows
for the API user to mix up lazy and eager arguments within the same logging method call.

- Lambda expressions of `Supplier` arguments have to be explicitly downcast. That is mandated by lambda syntax
  because the `Logger` API declares the lazy argument as an `Object` rather than a functional interface. No need of
  downcast if the `Supplier` argument is passed in as a reference instead of a lambda expression.

### For Logging Service API Users...

Note that ELF4J is a logging service facade and specification, rather than the implementation. As such,

#### No-op by Default

- Nothing will be logging out (no-op) unless a properly configured external ELF4J service provider is discovered at the
  application start time. The ELF4J facade itself only ships with a default no-op logging provider.

#### Only One In-effect Logging Provider

- The API user can select or change to
  use any [ELF4J service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-the-elf4j-spi)
  at deploy time, without code change.
- The recommended setup is to ensure that only the one desired logging provider JAR be present in the classpath; or no
  external provider JAR if no-op is desired. In this case, nothing further is needed for ELF4J to work.
- If multiple external provider JARs are present, somehow, then the system property `elf4j.logger.factory.fqcn` has to
  be used to select the desired provider. No-op applies if the specified provider JAR is absent from the classpath.

  ```
  java -Delf4j.logger.factory.fqcn="elf4j.log4j.Log4jLoggerFactory" -jar MyApplication.jar
  ```

  With the provided no-op logger factory, this system property can also be used to turn OFF (no-op) all logging service
  discovered by the ELF4J facade:

  ```
  java -Delf4j.logger.factory.fqcn="elf4j.util.NoopLoggerFactory" -jar MyApplication.jar
  ```

- It is considered a setup error to have multiple provider JARs in the classpath without a selection. The ELF4J facade
  falls back to no-op in all error scenarios related to logging service provider discovery.

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

### For Logging Service Providers...

As with the Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html),
the logging _service provider_ implementation should be concrete and complete - including both the _provider class_ for
the `LoggerFactory` [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) and the _service class_ for
the `Logger` API - and packaged with the proper _provider-configuration_ file in the resource
directory `META-INF/services` as specified by `java.util.ServiceLoader`.

## Available Logging Service Providers of the ELF4J SPI

- A native ELF4J provider implementation: [elf4j-impl](https://github.com/elf4j/elf4j-impl)
- [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
- [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
- [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
- [Java 4 (java.util.logging) provider](https://github.com/elf4j/elf4j-jul)
- [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)
- ...

![Visitor Count](https://profile-counter.glitch.me/elf4j/count.svg)
