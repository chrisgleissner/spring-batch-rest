# spring-batch-rest

[![Build Status](https://travis-ci.org/chrisgleissner/spring-batch-rest.svg?branch=master)](https://travis-ci.org/chrisgleissner/spring-batch-rest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.chrisgleissner/spring-batch-rest-api/badge.svg)](https://search.maven.org/artifact/com.github.chrisgleissner/spring-batch-rest-api)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/spring-batch-rest/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/spring-batch-rest?branch=master)

REST API for Spring Batch, based on Spring MVC and Spring Boot.

## Features
- Get information on jobs, job executions, and Quartz schedules
- Start job execution (synchronous or asynchronous) with optional job property overrides. The job properties can
either be obtained via a custom API or via standard Spring Batch job parameters, accessible from <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/scope/StepScope.html">step-scoped beans</a>.

## Getting Started

To integrate the REST API in your Spring Boot project do the following:

Add a dependency to your pom.xml:
```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>spring-batch-rest-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Add `@EnableSpringBatchRest` to your Spring Boot application class, for <a href="https://github.com/chrisgleissner/spring-batch-rest/blob/master/test/src/main/java/com/github/chrisgleissner/springbatchrest/example/SpringBatchRestSampleApplication.java">example</a>:
```java
@SpringBootApplication
@EnableSpringBatchRest
public class SpringBatchRestSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestSampleApplication.class, args);
    }
}
```

## REST Endpoints

The following REST endpoints are available:

#### Jobs

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobs                  | All jobs  |
| GET          | /jobs/{jobName}        | Single job  |

#### Job Executions

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobExecutions         | All job executions |
| GET          | /jobExecutions/{id}    | Single job execution |
| POST         | /jobExecutions         | Start job execution |

#### Quartz Schedules

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobDetails            | All Quartz schedules   |
| GET          | /jobsDetails/{quartzGroupName}/{quartzJobName}  | Single Quartz schedule |

#### Swagger Docs

Full Swagger API docs can be found at 
<a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a> after running:
```text
mvn clean install
java -jar example/target/spring-batch-rest-example*.jar
```


## Job Property Overrides

Properties can be overridden when starting a job via REST. These overrides can then be accessed from within a job:
```java
@Bean
ItemWriter<Object> writer() {
    return new ItemWriter<Object>() {
        @Override
        public void write(List<?> items) throws Exception {
           String prop = JobPropertyResolvers.JobProperties.of("jobName").getProperty("propName");
           // ...
        }
    }
}
```

An alternative approach is to use job parameters on `@StepScope`-annotated beans:
```java
@StepScope
@Bean 
ItemWriter<Object> writer(@Value("#{jobParameters['propName']}") String prop) {
    // ... 
}
```

If a property is not overridden, it is resolved against the Spring environment. All overrides are reverted on job completion.

## Utilities

This project also contains utilities for starting or scheduling Spring Batch jobs ad-hoc, with minimal use of Spring.

[JobBuilder](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/JobBuilder.java)

Build a simple job based on a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runnable.html">Runnable</a>:

```java
Job job = jobBuilder.createJob("jobName", () -> System.out.println("Running job"));
```

[AdHocScheduler](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocScheduler.java)

Ad-hoc scheduler to register and trigger a job using a Quartz CRON trigger. This can be performed at 
run-time rather than Spring wiring time which allows for simplified set-up of a large number of jobs that only 
differ slightly:

```java
adHocScheduler.schedule("jobName", job, "0/30 * * * * ?");
```

[AdHocStarter](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/adhoc/AdHocStarter.java)

Similar to AdHocScheduler, but for immediately starting a job:

```java
adHocStarter.start("jobName", job);

```

