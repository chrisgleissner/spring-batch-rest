package com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail;

import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class JobDetailResource extends RepresentationModel<JobDetailResource> {
    private JobDetail jobDetail;

    // For Jackson
    private JobDetailResource() {
    }

    public JobDetailResource(final JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        add(linkTo(methodOn(JobDetailController.class).get(jobDetail.getQuartzGroupName(), jobDetail.getQuartzJobName())).withSelfRel());
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }
}
