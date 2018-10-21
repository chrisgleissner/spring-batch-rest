package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


public class JobExecutionResource extends ResourceSupport {
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
