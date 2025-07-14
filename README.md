[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j%22)

# elf4j - Easy Logging Facade for Java

API and SPI of a no-fluff Java logging facade

* ... because sometimes the wheel should be reinvented.

## User stories

1. As a Java application developer, I want to use a log service API, so that I can choose or switch to use any compliant log service provider, at application deployment time without code change or re-compile.
2. As a log service/engine/framework provider, I want to implement a Service Provider Interface [(SPI)](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), so that the log service client can opt to use my service implementation, at application deployment time without code change or re-compile.

## Prerequisite

Java 8 or better, although individual logging service providers may have higher JDK version prerequisite.

## What it is...

### It is a logging Service Interface and Access API

If you are familiar with other logging APIs such as SLF4J/LOGBACK/LOG4J, you will find most counterpart logging methods in ELF4J, with some noticeable differences:

* When logging a Throwable/Exception, the Throwable always goes as the very first argument in the logging method's multi-argument signature (as opposed to the last as in the other APIs).
* As the logging service access API, the static factory method `Logger.instance()` does not take any argument. If needed, it is up to the logging service provider to detect the declaring/caller class and decide the default name and severity level of the logger instance to be returned.
* The severity level of a `Logger` instance is immutable. Although the `Logger` instance can log at any level via the convenience `Logger.<level>()` methods, the `Logger.log()` methods always log at the level of the current `Logger` instance. The instance factory methods `Logger.at<Level>()` return a different `Logger` instance if the specified level is different from the current instance's.

```java
/**
 * The Logger serves as both the "service interface" and "access API" as in the <a
 * href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">Java Service Provider Framework</a>.
 *
 * <p>All {@link Logger} instances from this API should be thread-safe.
 */
public interface Logger {
  /**
   * Static factory method as the "service access API" that provides a default Logger instance
   *
   * @return Logger instance with default name and Level
   * @implNote It is up to the logging service provider to determine the default name and level of the logger instance
   *     to be returned.
   */
  static Logger instance() {
    return LogServiceProviderLocator.INSTANCE.logServiceProvider().logger();
  }

  /**
   * Instance factory method that provides a Logger instance for the specified log level with the same name as this
   * Logger instance
   *
   * @param level the logging level of the requested Logger instance
   * @return Logger instance of the specified level
   * @implNote A Logger instance's severity level is immutable and cannot be changed after creation. Therefore, this
   *     method can return the current instance itself only if the specified level is the same as the current
   *     instance's; otherwise, it will have to be a different Logger instance to be returned.
   */
  Logger atLevel(Level level);

  /**
   * Retrieves the severity level of the Logger instance.
   *
   * @return the severity level of the logger instance
   */
  Level getLevel();

  /**
   * Checks if logging is enabled for this Logger instance.
   *
   * @return true if logging is enabled, false otherwise
   */
  boolean isEnabled();

  /**
   * Checks if logging is enabled for the Logger instance obtained by calling one of the instance factory methods of this Logger
   * instance at the specified level
   *
   * @param level the logging level to check
   * @return true if logging is enabled at the specified level, false otherwise
   */
  default boolean isEnabled(Level level) {
    return atLevel(level).isEnabled();
  }

  /**
   * Logs a message.
   *
   * @param message the message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
   *     {@link Supplier#get()} is used to construct the final log message.
   */
  void log(Object message);

  /**
   * Logs a message provided by a Supplier. Convenience overloading method of {@link #log(Object)}, so no need of
   * downcast to {@link Supplier} when the message argument is provided as a lambda expression.
   *
   * @param message Supplier of the message to be logged
   */
  default void log(@NonNull Supplier<?> message) {
    if (!isEnabled()) {
      return;
    }
    log(message.get());
  }

  /**
   * Logs a formatted message with arguments.
   *
   * @param message the message to be logged, which may contain argument placeholders denoted as `{}` tokens
   * @param arguments the arguments whose values will replace the placeholders in the message. The arguments can be a
   *     mixture of both eager {@code Object} and lazy {@code Supplier<?>} types. When both types are present, lambda
   *     expression arguments need to be downcast to {@code Supplier<?>} per the lambda syntax requirement.
   */
  void log(String message, Object... arguments);

  /**
   * Logs a formatted message with arguments provided by Suppliers.
   *
   * @param message the message to be logged
   * @param arguments Suppliers of the arguments to replace placeholders in the message; no downcast needed as all
   *     arguments are of {@code Supplier<?>} type.
   */
  default void log(String message, Supplier<?>... arguments) {
    if (!isEnabled()) {
      return;
    }
    log(message, supply(arguments));
  }

  static Object @NonNull [] supply(Supplier<?>[] arguments) {
    return Arrays.stream(arguments).map(Supplier::get).toArray(Object[]::new);
  }

  /**
   * Logs a Throwable.
   *
   * @param throwable the Throwable to be logged
   */
  void log(Throwable throwable);

  /**
   * Logs a Throwable with an accompanying message.
   *
   * @param throwable the Throwable to be logged
   * @param message the accompanying message to be logged. If the actual type is {@link java.util.function.Supplier},
   *     the result of {@link Supplier#get()} is used to compute the final log message.
   */
  void log(Throwable throwable, Object message);

  /**
   * Logs a Throwable with an accompanying message provided by a Supplier.
   *
   * @param throwable the Throwable to be logged
   * @param message Supplier of the accompanying message to be logged
   */
  default void log(Throwable throwable, @NonNull Supplier<?> message) {
    if (!isEnabled()) {
      return;
    }
    log(throwable, message.get());
  }

  /**
   * Logs a Throwable with a formatted message and arguments.
   *
   * @param throwable the Throwable to be logged
   * @param message the message to be logged, which may contain argument placeholders
   * @param arguments the arguments whose values will replace the placeholders in the message
   */
  void log(Throwable throwable, String message, Object... arguments);

  /**
   * Logs a Throwable with a formatted message and arguments provided by Suppliers.
   *
   * @param throwable the Throwable to be logged
   * @param message the message to be logged
   * @param arguments Suppliers of the arguments to replace placeholders in the message
   */
  default void log(Throwable throwable, String message, Supplier<?>... arguments) {
    if (!isEnabled()) {
      return;
    }
    log(throwable, message, supply(arguments));
  }

  // Following methods are convenience shorthands added to resemble other logging APIs

  default boolean isTraceEnabled() {
    return atTrace().isEnabled();
  }

  default void trace(Object message) {
    atTrace().log(message);
  }

  default void trace(Throwable throwable, String message, Object... arguments) { // again, the throwable goes first
    atTrace().log(throwable, message, arguments);
  }

  // More resembling convenience methods...
}
```

### It is a logging Service Provider Interface (SPI)

```java
public interface LogServiceProvider {
  Logger logger();
}
```

### Conventions, Defaults, and Implementation Notes (a.k.a. "the spec")

#### Thread safety

A `Logger` instance should be thread-safe.

#### Severity Level

If a Logger instance is obtained via the `Logger.instance()` static factory method, then the default severity level of such instance is decided by the _service provider_ implementation. If a Logger instance is obtained via one of the `Logger.at<Level>` instance factory methods, then its severity level should be as requested.

#### Placeholder token

The empty curly braces token `{}` should be the placeholder for message arguments. This is by convention, and does not syntactically appear in the API or SPI. Both the API user and the Service Provider must honor such convention.

#### Lazy arguments

Lazy arguments are those whose runtime type is `java.util.function.Supplier`. Compared to other types of arguments, lazy ones have to be treated specially in that the `Supplier` function must be applied first before the result is used as the substitution to the argument placeholder. This special handling of lazy arguments is by convention, and not syntactically enforced by the API or SPI. It allows for the API user to mix up lazy and eager arguments within the same logging method call.

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

### Use it as the logging Service (facade) API

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

  - The elf4j API user can select or change into using any [elf4j service provider](https://github.com/elf4j/elf4j#available-logging-service-providers-of-elf4j) at deploy time, without application code change or re-compile.
  - The recommended setup is to ensure that only one desired logging provider with its associated JAR(s) be present in the classpath; or, no provider JAR when no-op is desired. In this case, nothing further is needed for elf4j to work.
  - If multiple eligible providers are present in classpath, somehow, then the system property `elf4j.service.provider.fqcn` has to be used to select the desired provider. No-op applies if the specified provider is absent.

    ```bash
    java -Delf4j.service.provider.fqcn="elf4j.log4j.Log4jLoggerFactory" MyApplication
    ```

    With the default no-op logging provider, this system property can also be used to turn OFF all logging services discovered by the elf4j facade:

    ```bash
    java -Delf4j.service.provider.fqcn="elf4j.util.NoopLogServiceProvider" MyApplication
    ```

  - It is considered a setup error to have multiple providers in the classpath without a selection. The elf4j facade falls back to no-op on any setup errors.

### Use it as the _service provider interface_ (SPI) to provide the logging service

To enable an independent logging framework/engine via the elf4j spec, the _service provider_ should follow instructions of Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html). Namely, the implementation should include

* the _provider class_ implementing the `LogServiceProvider` [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), the _service class_ implementing the `Logger` API, and their associated classes as needed
* the _provider-configuration_ file, named `elf4j.spi.LogServiceProvider` in the resource directory `META-INF/services`, whose content is the Fully Qualified Name of the SPI _provider class_ implementing the `LogServiceProvider` SPI interface

### Available logging _service providers_ of elf4j
* A native elf4j service provider: [elf4j-provider](https://github.com/elf4j/elf4j-provider)
* [tinylog provider](https://github.com/elf4j/elf4j-tinylog)
* [LOG4J provider](https://github.com/elf4j/elf4j-log4j)
* [LOGBACK provider](https://github.com/elf4j/elf4j-logback)
* [Java Util Logging (java.util.logging) provider](https://github.com/elf4j/elf4j-jul)
* [SLF4J provider](https://github.com/elf4j/elf4j-slf4j)