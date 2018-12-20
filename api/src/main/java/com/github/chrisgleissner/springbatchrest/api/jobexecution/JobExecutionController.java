package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/jobExecutions", produces = "application/hal+json")
public class JobExecutionController {

    @Autowired
    private JobExecutionService jobExecutionService;

    @GetMapping("/{id}")
    public JobExecutionResource get(@PathVariable long id) {
        return new JobExecutionResource(jobExecutionService.jobExecution(id));
    }

    @GetMapping
    public Resources<JobExecutionResource> all(
            @RequestParam(value = "jobName", required = false) String jobName,
            @RequestParam(value = "exitCode", required = false) String exitCode,
            @RequestParam(value = "maxNumberOfJobInstances", required = false) Integer maxNumberOfJobInstances,
            @RequestParam(value = "maxNumberOfJobExecutionsPerInstance", required = false) Integer maxNumberOfJobExecutionsPerInstance) {
        Collection<JobExecutionResource> jobExecutions = jobExecutionService.jobExecutions(
                Optional.ofNullable(jobName),
                Optional.ofNullable(exitCode),
                Optional.ofNullable(maxNumberOfJobInstances),
                Optional.ofNullable(maxNumberOfJobExecutionsPerInstance)).stream().map(JobExecutionResource::new).collect(Collectors.toList());
        return new Resources<>(jobExecutions, linkTo(methodOn(JobExecutionController.class)
                .all(jobName, exitCode, maxNumberOfJobInstances, maxNumberOfJobExecutionsPerInstance)).withSelfRel());
    }


    @PostMapping
    public ResponseEntity<JobExecutionResource> put(@RequestBody JobConfig jobConfig) {
        JobExecutionResource resource = new JobExecutionResource(jobExecutionService.launch(jobConfig));
        boolean failed = resource.getJobExecution().getExitCode().equals(ExitStatus.FAILED.getExitCode());
        return new ResponseEntity<>(resource, failed ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }
}
