package com.github.chrisgleissner.springbatchrest.api.job;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Api(tags = "Spring Batch Jobs")
@RestController
@RequestMapping(value = "/jobs", produces = "application/hal+json")
public class JobController {

    @Autowired
    private JobService jobService;

    @ApiOperation("Get a Spring Batch job by name")
    @GetMapping("/{jobName}")
    public JobResource get(@PathVariable String jobName) {
        return new JobResource(jobService.job(jobName));
    }

    @ApiOperation("Get all Spring Batch jobs")
    @GetMapping
    public CollectionModel<JobResource> all() {
        Collection<JobResource> jobs = jobService.jobs().stream().map(JobResource::new).collect(toList());
        return new CollectionModel<>(jobs, linkTo(methodOn(JobController.class).all()).withSelfRel());
    }
}
