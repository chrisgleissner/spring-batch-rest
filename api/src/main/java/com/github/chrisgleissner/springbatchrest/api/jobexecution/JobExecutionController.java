package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Api(tags = "Spring Batch Job Executions")
@RestController
@RequestMapping(value = "/jobExecutions", produces = "application/hal+json")
public class JobExecutionController {

    @Autowired
    private JobExecutionService jobExecutionService;

    @ApiOperation("Get all Spring batch job execution by ID")
    @GetMapping("/{id}")
    public JobExecutionResource get(@PathVariable long id) {
        return new JobExecutionResource(jobExecutionService.jobExecution(id));
    }

    @ApiOperation("Find Spring batch job executions by job name and exit code")
    @GetMapping
    public CollectionModel<JobExecutionResource> all(
            @RequestParam(value = "jobName", required = false) String jobName,
            @RequestParam(value = "exitCode", required = false) String exitCode,
            @RequestParam(value = "limitPerJob", defaultValue = "3") Integer limitPerJob) {
        Collection<JobExecutionResource> jobExecutions = jobExecutionService.jobExecutions(
                optional(jobName),
                optional(exitCode),
                limitPerJob).stream().map(JobExecutionResource::new).collect(toList());
        return new CollectionModel<>(jobExecutions, linkTo(methodOn(JobExecutionController.class)
                .all(jobName, exitCode, limitPerJob)).withSelfRel());
    }

    private Optional<String> optional(String s) {
        if (s != null) {
            s = s.trim();
            if (s.isEmpty())
                s = null;
        }
        return Optional.ofNullable(s);
    }

    @ApiOperation("Start a Spring Batch job execution")
    @PostMapping
    public ResponseEntity<JobExecutionResource> put(@RequestBody JobConfig jobConfig) {
        JobExecutionResource resource = new JobExecutionResource(jobExecutionService.launch(jobConfig));
        boolean failed = resource.getJobExecution().getExitCode().equals(ExitStatus.FAILED.getExitCode());
        return new ResponseEntity<>(resource, failed ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }
}
