# spring-batch-rest

[![Build Status](https://travis-ci.org/chrisgleissner/spring-batch-rest.svg?branch=master)](https://travis-ci.org/chrisgleissner/spring-batch-rest)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/spring-batch-rest/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/spring-batch-rest?branch=master)

REST API for Spring Batch, based on Spring MVC and Spring Boot.

### Features
- Get information on jobs, job executions, and Quartz schedules
- Start job execution (synchronous or asynchronous) with optional job property overrides. The job properties can
either be obtained via a custom API or via standard Spring Batch job parameters, accessible from <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/scope/StepScope.html">step-scoped beans</a>.

### Getting Started

To integrate the REST API in your Spring Batch project, do the following:

Add a dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>spring-batch-rest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Add the following to the root configuration class which drives your Spring wiring:

```java
@EnableBatchProcessing
@EnableSwagger2
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
```

### REST Endpoints

Documentation for all REST endpoints can be found at http://localhost:8080/swagger-ui.html after starting your application.

### Specifying Job Properties via REST

Job properties which are specified when triggering a job via REST can be accessed via a custom API. This API delegates the actual
property resolution to the Spring <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/env/PropertyResolver.html">PropertyResolver<a>.

Example:
```java
String myProperty = JobPropertyResolvers.JobProperties.of("myJobName").getProperty("myPropertyName");
int myIntProperty = JobPropertyResolvers.JobProperties.of("myJobName").getProperty("myIntPropertyName", Integer.class, 2);
```

## Util

This project also contains utilities for starting or scheduling Spring Batch jobs ad-hoc, with minimal use of Spring.

[AdHocScheduler](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocScheduler.java)

Ad-hoc scheduler to register and trigger Spring Batch jobs using Quartz CRON triggers at run-time rather than Spring
wiring time. This allows for simplified set-up of a large number of jobs that only differ slightly:

```java
adHocScheduler.schedule("jobName", job, "0/30 * * * * ?");
```

[AdHocStarter](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocStarter.java)

Ad-hoc starter to immediately launch jobs:

```java
adHocStarter.start("jobName", job);

```

[JobBuilder](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/JobBuilder.java)

Build a simple job based on a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runnable.html">Runnable</a>:

```java
jobBuilder.createJob("jobName", () -> System.out.println("Running job"));

```

