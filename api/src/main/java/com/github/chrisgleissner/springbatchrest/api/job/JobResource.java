package com.github.chrisgleissner.springbatchrest.api.job;

import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class JobResource extends ResourceSupport {
    private Job job;

    // For Jackson
    private JobResource() {}

    public JobResource(final Job job) {
        this.job = job;
        add(linkTo(methodOn(JobController.class).get(job.getName())).withSelfRel());
    }

    public Job getJob() {
        return job;
    }
}
