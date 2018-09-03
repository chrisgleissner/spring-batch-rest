package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class JobExecutionResource extends ResourceSupport {
    private final JobExecution jobExecution;

    public JobExecutionResource(final JobExecution jobExecution) {
        this.jobExecution = jobExecution;
        add(linkTo(methodOn(JobExecutionController.class).get(jobExecution.getId())).withSelfRel());
    }
}
