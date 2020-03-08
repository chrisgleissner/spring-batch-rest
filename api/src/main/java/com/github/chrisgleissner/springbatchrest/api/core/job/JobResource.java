package com.github.chrisgleissner.springbatchrest.api.core.job;

import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class JobResource extends RepresentationModel<JobResource> {
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
