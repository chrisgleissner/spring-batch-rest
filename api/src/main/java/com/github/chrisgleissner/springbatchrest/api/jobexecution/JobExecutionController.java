package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/jobExecution", produces = "application/hal+json")
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
            @RequestParam(value = "exitStatus", required = false) ExitStatus exitStatus,
            @RequestParam(value = "maxNumberOfJobInstances", required = false) Integer maxNumberOfJobInstances,
            @RequestParam(value = "maxNumberOfJobExecutionsPerInstance", required = false) Integer maxNumberOfJobExecutionsPerInstance) {
        Collection<JobExecutionResource> jobExecutions = jobExecutionService.jobExecutions(
                Optional.ofNullable(jobName),
                Optional.ofNullable(exitStatus),
                Optional.ofNullable(maxNumberOfJobInstances),
                Optional.ofNullable(maxNumberOfJobExecutionsPerInstance)).stream().map(JobExecutionResource::new).collect(Collectors.toList());
        return new Resources<>(jobExecutions, linkTo(methodOn(JobExecutionController.class)
                .all(jobName, exitStatus, maxNumberOfJobInstances, maxNumberOfJobExecutionsPerInstance)).withSelfRel());
    }
}
