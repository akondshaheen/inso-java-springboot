package com.inso.learning.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =============================================================================
 * THE APPLICATION ENTRY POINT
 * =============================================================================
 *
 * WHAT IS THE JVM, JDK AND JRE?
 * -------------------------------------------------------------------------
 * - We write Java source code in ".java" files.
 * - The JDK (Java Development Kit) contains the "javac" compiler, which
 *   turns our source code into ".class" files containing "bytecode" - an
 *   intermediate format that any JVM on any operating system can run.
 * - The JRE (Java Runtime Environment) contains everything needed to RUN
 *   compiled bytecode: the JVM plus the core Java class libraries. The JDK
 *   includes a JRE, plus the developer tools needed to CREATE programs.
 * - The JVM (Java Virtual Machine) loads our ".class" files ("class
 *   loading"), verifies the bytecode, and executes it, translating it into
 *   real CPU instructions as it runs (Just-In-Time compilation). This is
 *   why the same .class file runs unchanged on Windows, Linux, or macOS -
 *   each platform has its own JVM able to run that same bytecode.
 *
 * WHAT DOES @SpringBootApplication DO?
 * -------------------------------------------------------------------------
 * This single annotation is a shortcut for three annotations:
 *   1. @SpringBootConfiguration - marks this class as a source of bean
 *      definitions and configuration (a specialised @Configuration).
 *   2. @EnableAutoConfiguration - tells Spring Boot to look at the
 *      dependencies on the classpath and automatically configure sensible
 *      defaults. Because "spring-boot-starter-webmvc" is on the classpath,
 *      Spring Boot automatically starts an embedded Tomcat server and sets
 *      up Spring MVC, without us writing any manual server configuration.
 *   3. @ComponentScan - tells Spring to search this class's package (and
 *      every sub-package: controller, service, repository, entity,
 *      domain, mapper, ...) for classes annotated with @Component,
 *      @Service, @Repository, or @Controller/@RestController, and
 *      register them as "beans" it manages. This is why every class in
 *      this project must live under "com.inso.learning.taskflow" - if it
 *      lived outside this package tree, Spring would never find it.
 *
 * WHAT IS A "SPRING BEAN", AND WHAT HAPPENS WHEN SPRING SEES @Service?
 * -------------------------------------------------------------------------
 * A "bean" is an object whose creation and lifecycle are managed by
 * Spring's ApplicationContext (a container), instead of us calling "new"
 * ourselves. When component scanning finds a class annotated with
 * @Service (or @Component, @Repository, @Controller), Spring registers
 * that class as a bean definition. When the context starts, Spring
 * creates an instance of the class, looks at its constructor to see what
 * OTHER beans it needs, finds or creates those first, injects them, and
 * stores the finished object in the ApplicationContext so it can be
 * injected into any other bean that needs it. This is Dependency
 * Injection (DI) - Spring is not "magic", it is simply reading annotations
 * with Java Reflection at startup and building an object graph for us
 * according to rules we can always read about and predict.
 *
 * Inversion of Control (IoC) is the broader principle behind this: instead
 * of OUR code deciding when to create objects and how to wire them
 * together, we hand that control over to the Spring framework, and just
 * describe WHAT we need (through constructor parameters and annotations).
 *
 * WHAT HAPPENS WHEN main() RUNS?
 * -------------------------------------------------------------------------
 * SpringApplication.run(...) does roughly the following, in order:
 *   1. Creates the ApplicationContext (the bean container).
 *   2. Performs component scanning across this package tree.
 *   3. Reads application.yml and applies any matching configuration.
 *   4. Runs auto-configuration (embedded Tomcat, a JPA
 *      EntityManagerFactory connected to the configured datasource, ...).
 *   5. Instantiates every bean, resolving constructor dependencies between
 *      them - this is Dependency Injection actually happening, layer by
 *      layer: entities need nothing extra, mappers need other mappers,
 *      *JpaRepository interfaces get their dynamic proxy implementations,
 *      *RepositoryImpl classes get their JpaRepository + mapper, services
 *      get their repositories, and controllers get their services.
 *   6. Starts the embedded Tomcat server so the application can accept
 *      HTTP requests (default port 8080).
 *
 * WHY IS CACHING CONFIGURED IN A SEPARATE config/CacheConfig CLASS,
 * INSTEAD OF RIGHT HERE?
 * -------------------------------------------------------------------------
 * See config/CacheConfig's Javadoc for the full explanation - in short,
 * @WebMvcTest and @DataJpaTest (our narrow test slices) still treat THIS
 * class as their configuration source, so any annotation placed directly
 * on it (like @EnableCaching) would affect those tests too, even though
 * they never need caching. Keeping @EnableCaching on its own,
 * separately-scanned @Configuration class avoids that.
 */
@SpringBootApplication
public class TaskFlowApplication {

    /**
     * The main method is the standard Java entry point - the JVM always
     * looks for a method with exactly this signature to start a program.
     * SpringApplication.run() bootstraps the whole Spring Boot application
     * as described above and keeps the embedded server running so it can
     * accept requests until the process is stopped.
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }
}
