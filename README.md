# spring-batch-rest

[![Maven Central](https://img.shields.io/maven-central/v/com.github.chrisgleissner/spring-batch-rest-api)](https://search.maven.org/artifact/com.github.chrisgleissner/spring-batch-rest-api/)
[![Javadocs](https://www.javadoc.io/badge/com.github.chrisgleissner/spring-batch-rest-api.svg)](https://www.javadoc.io/doc/com.github.chrisgleissner/spring-batch-rest-api)
[![Build Status](https://travis-ci.org/chrisgleissner/spring-batch-rest.svg?branch=master)](https://travis-ci.org/chrisgleissner/spring-batch-rest)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/spring-batch-rest/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/spring-batch-rest?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/2267ddd7cbbfc5e22b86/maintainability)](https://codeclimate.com/github/chrisgleissner/spring-batch-rest/maintainability)

REST API for <a href="https://spring.io/projects/spring-batch">Spring Batch</a> based on <a href="https://github.com/spring-projects/spring-boot">Spring Boot 2.2</a> and <a href="https://github.com/spring-projects/spring-hateoas">Spring HATOEAS</a>. It comes with an OpenAPI 3 documentation provided by <a href="https://github.com/springdoc/springdoc-openapi">Springdoc</a>.

Supports Java 8 and above. Tested on OpenJDK 8, 11, and 14.

## Features
- Get information on jobs, job executions, and Quartz schedules
- Start job execution (synchronous or asynchronous) with optional job property overrides. The job properties can
either be obtained via a custom API or via standard Spring Batch job parameters, accessible from <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/scope/StepScope.html">step-scoped beans</a>.

## Live Demo

Check out the <a href="https://spring-batch-rest.herokuapp.com/swagger-ui.html">live demo</a> of this project's Swagger UI. This demo is automatically updated whenever the repo's master branch changes. Please note that it may take up to 30s for this Heroku app to perform a cold start after it has not been used for a while.

Here's how to run a <a href="https://github.com/chrisgleissner/spring-batch-rest/tree/master/example/api/src/main/java/com/github/chrisgleissner/springbatchrest/example/core/PersonJobConfig.java">sample job<a>:
* Click on `Spring Batch Job Executions`, then on `POST`. Now click on `Try it Out` on the right-hand side. Replace the contents of the `Example Value` input field with `{"name": "personJob"}` and press `Execute`. 
* The job has now been triggered. When it completes, you'll get a response body similar to:
```
{
  "jobExecution": {
    "id": 1,
    "jobId": 1,
    "jobName": "personJob",
    "startTime": "2018-12-23T18:19:13.185",
    "endTime": "2018-12-23T18:19:13.223",
    "exitCode": "COMPLETED",
    "exitDescription": "",
    "status": "COMPLETED",
    "exceptions": []
  },
  "_links": {
    "self": {
      "href": "https://spring-batch-rest.herokuapp.com/jobExecutions/1"
    }
  }
}
```
* You can now view this and other recently completed jobs via the <a href="https://spring-batch-rest.herokuapp.com/jobExecutions?jobName=personJob&limitPerJob=3">job execution overview<a>:
```
{
  "_embedded": {
  "jobExecutionResourceList": [
    {
    "jobExecution": {
      "id": 1,
      "jobId": 1,
      "jobName": "personJob",
...
```

## Getting Started

To integrate the REST API in your Spring Boot project, first ensure you have an entry point to your application such as
 
```java
@SpringBootApplication
public class SpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }
}
``` 
 
Then, simply add one of the following two dependencies to your project:

### Core API

The `spring-batch-rest-api` dependency comes with `jobs` and `jobExecutions` REST endpoints. It is recommended if you
don't require Quartz for scheduling your jobs.

Maven:
```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>spring-batch-rest-api</artifactId>
    <version>1.5.0</version>
</dependency>
```

Gradle:
```
implementation 'com.github.chrisgleissner:spring-batch-rest-api:1.5.0'
```

### Quartz API

The `spring-batch-rest-quartz-api` dependency includes everything above and and additionally exposes Quartz schedules 
via the `jobDetails` REST endpoint.

Maven:
```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>spring-batch-rest-quartz-api</artifactId>
    <version>1.5.0</version>
</dependency>
```

Gradle:
```
implementation 'com.github.chrisgleissner:spring-batch-rest-quartz-api:1.5.0'
```

### See it in Action

To see `spring-batch-rest-api` in action, run
```text
mvn install -Dmaven.test.skip; java -jar example/api/target/*.jar
```

For `spring-batch-rest-quartz-api`, run
```text
mvn install -Dmaven.test.skip; java -jar example/quartz-api/target/*.jar
```
 
Once it's up, check the Swagger REST API docs at 
<a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>.


## REST Endpoints

The following REST endpoints are available:

### Jobs

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobs                  | All jobs  |
| GET          | /jobs/{jobName}        | Single job  |

### Job Executions

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobExecutions         | Latest 3 executions for each job, sorted by descending end time (or start time if still running) |
| GET          | /jobExecutions/{id}    | Single job execution |
| POST         | /jobExecutions         | Start job execution with optional property overrides |

#### Request Parameters for GET `/jobExecutions` 

| Parameter | Default Value | Description |
|-----------|---------------|-------------|
| jobName | empty | <a href="https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html">Regular expression</a> of the job names to consider. If empty, all job names are used. |
| exitCode | empty | Exit code of the job execution. Can be `COMPLETED`, `EXECUTING`, `FAILED`, `NOOP`, `STOPPED` or `UNKNOWN` as per <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/ExitStatus.html">ExitStatus</a>. If empty, all exit codes are used. |
| limitPerJob | 3 | Maximum number of job executions to return for each job. |

#### Examples

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobExecutions?limitPerJob=50  | Latest 50 executions for each job |
| GET          | /jobExecutions?jobName=foo&exitCode=FAILED | Latest 3 failed executions for 'foo' job |
| GET          | /jobExecutions?jobName=foo.*&exitCode=FAILED&limitPerJob=10 | Latest 10 failed executions for jobs with a name starting with 'foo' |

### Quartz Schedules

As mentioned above, these endpoints are only exposed if you're using the `spring-batch-rest-quartz-api` dependency:

| HTTP Method  | Path                   | Description  |
|--------------|------------------------|--------------|
| GET          | /jobDetails            | All Quartz schedules   |
| GET          | /jobsDetails/{quartzGroupName}/{quartzJobName}  | Single Quartz schedule |

## Error Handling

Where possible, subclasses of the Spring Batch <a href="https://docs.spring.io/spring-batch/trunk/apidocs/org/springframework/batch/core/JobExecutionException.html">JobExecutionException</a>
are mapped to an appropriate HTTP status code and the response body contains further details. 

For example, trying to start a nonexistent job results in a response with a 404 status code and the following response body:
```
{
  "status": "404 NOT_FOUND",
  "message": "No job configuration with the name [foo] was registered",
  "exception": "NoSuchJobException",
  "detail": "Failed to start job 'foo' with JobConfig(name=foo, properties={foo=baz10}, asynchronous=false). Reason: No job configuration with the name [foo] was registered"
}
```

## Configuration

The default behaviour of the REST API can be tweaked via several Spring properties which can be placed in `application.properties`.

### Job Execution Caching

`com.github.chrisgleissner.springbatchrest.jobExecutionCacheSize` (default: 100)

For performance reasons, `/jobExecutions` queries are performed against an in-memory cache of recent 
job executions. If the `limitPerJob` request parameter is larger than the value of this property, the cache is bypassed and the
Spring Batch <a href="https://docs.spring.io/spring-batch/4.0.x/api/index.html?org/springframework/batch/core/explore/JobExplorer.html">JobExplorer</a> is used instead.

Large `jobExecutionCacheSize` values will result in increased heap use, but small values combined with large `limitSize` request parameters
will cause increased REST query latencies. Thus, if you increase this property value, you may also want to increase your heap size. 

The cache only contains job executions since the Spring context creation, ie. it is not warmed up from the JobExplorer and the DB this may rely on. If you want to be able to query job executions that were performed earlier, eg. during a prior JVM execution, you may want to disable caching. To do so, simply set the property to 0.


### Repeated Job Execution

`com.github.chrisgleissner.springbatchrest.addUniqueJobParameter` (default: true)

Spring Batch prevents repeated invocations of a job unless you use different properties (aka job parameters) each time. To bypass this, a unique property (ie. a random UUID) is added to each job invocation. You can disable this by setting the property to false. 

### Disable Spring Batch REST API REST Endpoints

`com.github.chrisgleissner.springbatchrest.enabled=false` (default: true)

Useful if you only want to expose the REST API in certain environments.

### Disable Swagger UI 

`springdoc.swagger-ui.enabled=false` (default: true)

See https://github.com/springdoc/springdoc-openapi for further config options.

### Disable Custom Exception Handling 

`com.github.chrisgleissner.springbatchrest.controllerAdvice=false` (default: true)

This disables the global exception handling via `com.github.chrisgleissner.springbatchrest.api.core.jobexecution.ResponseExceptionHandler`. 

## Job Property Overrides

Properties can be overridden when starting a job via REST. You can then access these overrides in one of the following ways.

### @Value

Annotate your Spring bean method with `@StepScope` and use the `@Value` annotation on a method parameter 
to specify the desired job parameter name. 

Please note that this approach won't transparently fall back to Spring environment properties. Thus,
if this is desired, you should manually check if a job parameter is `null` and in this case return it from the Spring `Environment` instance. 

Example:
```java
@Bean @StepScope  
ItemWriter<Object> writer(@Value("#{jobParameters['sampleProperty']}") String sampleProperty) {
    // ... 
}
```

### PropertyResolver

When using `AdhocStarter`, you can create a `Job` using a `JobBuilder` and pass in a `Consumer<PropertyResolver>`. 

Properties looked up from this `PropertyResolver` transparently fall back to the Spring environment if properties can't be found in the job parameters.

Example:
```java
Job job = jobBuilder.createJob("sampleJob", propertyResolver -> {
    String propertyValue = propertyResolver.getProperty("sampleProperty");
    // ...
});
```

### JobProperties

In case you don't execute the same job concurrently, you may also look up properties from the `JobProperties` singleton. 

Properties looked up from this singleton transparently fall back to the Spring environment if properties can't be found in the 
job parameters. 

This approach is *deprecated* as it doesn't work with concurrent execution of the same job. Therefore, it is recommended to use one
of the other approaches.

Example:
```java
@Bean
ItemWriter<Object> writer() {
    return new ItemWriter<Object>() {
        @Override
        public void write(List<?> items) throws Exception {
           String sampleProperty = JobPropertyResolvers.JobProperties.of("sampleJob").getProperty("sampleProperty");
           // ...
        }
    }
}
```

## Utilities

The <a href="https://github.com/chrisgleissner/spring-batch-rest/tree/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util">util module</a> contains code for registering, starting and scheduling jobs:

[JobBuilder](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/core/JobBuilder.java) builds a simple job based on a <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runnable.html">Runnable</a>:

```java
Job job = jobBuilder.createJob("jobName", () -> System.out.println("Running job"));
```

[AdHocScheduler](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/quartz/AdHocScheduler.java) registers and triggers a job using a Quartz CRON trigger. This can be performed at 
run-time rather than Spring wiring time which allows for simplified set-up of a large number of jobs that only 
differ slightly:

```java
adHocScheduler.schedule("jobName", job, "0/30 * * * * ?");
```

[AdHocStarter](https://github.com/chrisgleissner/spring-batch-rest/blob/master/util/src/main/java/com/github/chrisgleissner/springbatchrest/util/core/AdHocStarter.java) is similar to AdHocScheduler, but used for immediately starting a job:

```java
adHocStarter.start(job);

```
