[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

# elf4j - Easy Logging Facade for Java

API and SPI of a no-fluff Java logging facade

* ... because sometimes the wheel should be reinvented.

## User stories

1. As a Java application developer, I want to use a log service API, so that I can choose or switch to use any compliant log service provider without changing my application code.
2. As a log service/engine/framework provider, I want to implement a Service Provider Interface [(SPI)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), so that the log service client application can opt (either in or out) to use my log service implementation, without code change.

## Prerequisite

* Java 8 or better for versions prior to 7.0.0, exclusive.
* Java 11 or better for versions post 7.0.0, inclusive.

Note that individual logging service providers may require higher JDK versions.

## What it is...

### It is a logging Service Interface and Access API

If you are familiar with other logging APIs such as SLF4J/LOGBACK/LOG4J, you will find most counterpart logging methods in ELF4J, with some noticeable differences:

* When logging a Throwable/Exception, the Throwable always goes as the very first argument in the logging method's multi-argument signature (as opposed to the last as in the other APIs).
* As the logging service access API, the static factory method `Logger.instance()` does not take any argument. When needed, it is up to the log service provider to detect the access API caller class and decide the default Name and severity Level of the logger instance to be returned. As with a value-based Object, the Logger instance's name may be important for the log configuration and the service provider implementation. However, the logger name is insignificant on the service caller/client API level, and not part of the {@code Logger} interface.
* The severity Level of a `Logger` instance is immutable. Although the `Logger` instance can log at any level via the convenience `Logger.<level>()` methods, the `Logger.log()` methods are always logging at the level of the current `Logger` instance. The instance factory methods `Logger.at<Level>()` must return a different {@code Logger} instance if the requested \<Level\> is different from the current instance's.
```java
public interface Logger {
    static Logger instance() {
        return LoggerFactoryLocator.INSTANCE.getLoggerFactory().getLogger();
    }

    Logger atLevel(Level level);

    Level getLevel();

    boolean isEnabled();

    default boolean isEnabled(Level level) {
        return atLevel(level).isEnabled();
    }

    void log(Object message);

    default void log(Supplier<?> message) {
        if (!isEnabled()) {
            return;
        }
        log(message.get());
    }

    void log(String message, Object... arguments);

    default void log(String message, Supplier<?>... arguments) {
        if (!isEnabled()) {
            return;
        }
        log(message, supply(arguments));
    }

    private static Object[] supply(Supplier<?>[] arguments) {
        return Arrays.stream(arguments).map(Supplier::get).toArray(Object[]::new);
    }

    void log(Throwable throwable);

    void log(Throwable throwable, Object message);

    default void log(Throwable throwable, Supplier<?> message) {
        if (!isEnabled()) {
            return;
        }
        log(throwable, message.get());
    }

    void log(Throwable throwable, String message, Object... arguments);

    default void log(Throwable throwable, String message, Supplier<?>... arguments) {
        if (!isEnabled()) {
            return;
        }
        log(throwable, message, supply(arguments));
    }

    default Logger atTrace() {
        return atLevel(Level.TRACE);
    }

    default Logger atDebug() {
        return atLevel(Level.DEBUG);
    }

    default Logger atInfo() {
        return atLevel(Level.INFO);
    }

    default Logger atWarn() {
        return atLevel(Level.WARN);
    }

    default Logger atError() {
        return atLevel(Level.ERROR);
    }

    default boolean isTraceEnabled() {
        return atTrace().isEnabled();
    }

    default boolean isDebugEnabled() {
        return atDebug().isEnabled();
    }

    default boolean isInfoEnabled() {
        return atInfo().isEnabled();
    }

    default boolean isWarnEnabled() {
        return atWarn().isEnabled();
    }

    default boolean isErrorEnabled() {
        return atError().isEnabled();
    }

    default void trace(Object message) {
        atTrace().log(message);
    }

    default void trace(String message, Object... arguments) {
        atTrace().log(message, arguments);
    }

    default void trace(Supplier<?> message) {
        logSuppliedAtLevel(Level.TRACE, message);
    }

    private void logSuppliedAtLevel(Level level, Supplier<?> message) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(message.get());
    }

    default void trace(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.TRACE, message, arguments);
    }

    private void logSuppliedAtLevel(Level level, String message, Supplier<?>[] arguments) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(message, supply(arguments));
    }

    default void trace(Throwable throwable) {
        atTrace().log(throwable);
    }

    default void trace(Throwable throwable, Object message) {
        atTrace().log(throwable, message);
    }

    default void trace(Throwable throwable, String message, Object... arguments) {
        atTrace().log(throwable, message, arguments);
    }

    default void trace(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.TRACE, throwable, message);
    }

    private void logSuppliedAtLevel(Level level, Throwable throwable, Supplier<?> message) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(throwable, message.get());
    }

    default void trace(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.TRACE, throwable, message, arguments);
    }

    private void logSuppliedAtLevel(Level level, Throwable throwable, String message, Supplier<?>[] arguments) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(throwable, message, supply(arguments));
    }

  // More resembling convenience methods...
}
```

### It is a logging Service Provider Interface (SPI)

```java
public interface LoggerFactory {
    Logger getLogger();
}
```
See details in the section of "Use it as the service provider interface (SPI) to provide concrete log service".

### Conventions, Defaults, and Implementation Notes (a.k.a. "the spec")

#### Thread safety

A `Logger` instance should be thread-safe.

#### Severity Level

If a Logger instance is obtained via the `Logger.instance()` static factory method, then the default severity level of such instance is decided by the _service provider_ implementation. If a Logger instance is obtained via one of the `Logger.at<Level>` instance factory methods, then its severity level should be as requested.

#### Placeholder token

The empty curly braces token `{}` should be the placeholder for message arguments. This is by convention, and does not syntactically appear in the API or SPI. Both the API user and the Service Provider must honor such convention.

#### Lazy arguments

Lazy arguments are those whose runtime type is `java.util.function.Supplier`, often provided via lambda expressions. Unlike other/eager types of arguments, lazy ones have to be treated specially in that the `Supplier` function must be applied first before the result is used as the substitution to the argument placeholder `{}`. This special handling of lazy arguments is by convention, and not syntactically enforced by the API or SPI. It allows for the API user to mix up lazy and eager arguments within the same logging method call.

## Get it

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

Include it as a compile-scope dependency in a build tool compatible with Maven Dependency Management. e.g.

```xml
<dependency>
    <groupId>io.github.elf4j</groupId>
    <artifactId>elf4j</artifactId>
    <version>${version}</version>
</dependency>
```

## Use it... 

### Use it as the logging Service (facade) API (while hiding the concrete log service provider API from your application code)

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
  class supplierMessageAndArguments {
    Logger logger = Logger.instance();

    @Test
    void noDowncastNeededWhenAllMessageOrArgumentsAreSuppliers() {
      logger.log(
          () ->
              "No downcast needed when message or arguments are all of Supplier type, rather than mixed with Object types");
      logger.log("Message can have any number of {} type arguments", Supplier.class::getTypeName);
      logger.log(
          "Lazy arguments of {} type can be used to supply values that may be {}",
          Supplier.class::getTypeName,
          () -> "expensive to compute");
      Exception ex = new Exception("test ex for Suppliers");
      logger.log(ex, () -> "Exception log message can be a Supplier");
      logger.log(ex, "So can the {}'s {}", () -> "message", () -> "arguments");
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

* **No-op by default**

  - Nothing will be logging out (no-op) unless a properly configured [elf4j service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-elf4j) JAR is discovered at the application start time. The elf4j facade itself only ships with a default no-op logging provider.

* **Only one in-effect logging provider**

  - The elf4j API user can select or change into using any [elf4j service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-elf4j) without client application code change.
  - The recommended setup is to ensure that only one desired logging provider with its associated JAR(s) be present in the classpath; or, no provider JAR when no-op is desired. In this case, nothing further is needed for elf4j to work.
  - If multiple eligible providers are present in classpath, somehow, then the system property `elf4j.service.provider.fqcn` has to be used to select the desired provider. No-op applies if the specified provider is absent.

    ```bash
    java -Delf4j.service.provider.fqcn="elf4j.log4j.Log4jLoggerFactory" MyApplication
    ```

    With the default no-op logging provider, this system property can also be used to turn OFF all logging services discovered by the elf4j facade:

    ```bash
    java -Delf4j.service.provider.fqcn="elf4j.util.NoopLoggerFactory" MyApplication
    ```

  - It is considered a setup error to have multiple logger factory SPI implementations in the classpath without a selection. The elf4j facade falls back to no-op on any setup errors.

### Use it as the _service provider interface_ (SPI) to provide concrete log service

To enable an independent logging framework/engine via the elf4j spec, the _service provider_ should follow instructions of Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html). Namely, the implementation should include

* the _provider class_ implementing the `elf4j.spi.LoggerFactory` [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), including the _service class_ implementing the `Logger` API, and their associated classes as needed
* the _provider-configuration_ file, named `elf4j.spi.LoggerFactory` in the resource directory `META-INF/services`, whose content is the Fully Qualified Name of the implementing class for the `elf4j.spi.LoggerFactory` SPI interface

### Available logging _service providers_ of elf4j
* A native elf4j service provider: [elf4j-provider](https://github.com/elf4j/elf4j-provider)
* [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
* [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
* [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
* [Java Util Logging (java.util.logging) provider](https://github.com/elf4j/elf4j-jul)
* [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)
