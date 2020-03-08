package com.github.chrisgleissner.springbatchrest.api.core.jobexecution;

import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


public class JobExecutionResource extends RepresentationModel<JobExecutionResource> {
    private JobExecution jobExecution;

    // For Jackson
    private JobExecutionResource() {
    }

    public JobExecutionResource(final JobExecution jobExecution) {
        this.jobExecution = jobExecution;
        add(linkTo(methodOn(JobExecutionController.class).get(jobExecution.getId())).withSelfRel());
    }

    public JobExecution getJobExecution() {
        return jobExecution;
    }
}
