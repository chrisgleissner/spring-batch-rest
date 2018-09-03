package com.github.chrisgleissner.springbatchrest.api.job;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class JobResource extends ResourceSupport {
    private final Job job;

    public JobResource(final Job job) {
        this.job = job;
        add(linkTo(methodOn(JobController.class).get(job.getName())).withSelfRel());
    }
}
