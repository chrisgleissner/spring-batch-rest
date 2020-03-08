package com.github.chrisgleissner.springbatchrest.api.core.job;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static com.github.chrisgleissner.springbatchrest.api.core.Constants.REST_API_ENABLED;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ConditionalOnProperty(name = REST_API_ENABLED, havingValue = "true", matchIfMissing = true)
@RestController
@RequestMapping(value = "/jobs", produces = "application/hal+json")
public class JobController {

    @Autowired
    private JobService jobService;

    @Operation(summary = "Get a Spring Batch job by name")
    @GetMapping("/{jobName}")
    public JobResource get(@PathVariable String jobName) {
        return new JobResource(jobService.job(jobName));
    }

    @Operation(summary = "Get all Spring Batch jobs")
    @GetMapping
    public CollectionModel<JobResource> all() {
        Collection<JobResource> jobs = jobService.jobs().stream().map(JobResource::new).collect(toList());
        return new CollectionModel<>(jobs, linkTo(methodOn(JobController.class).all()).withSelfRel());
    }
}
