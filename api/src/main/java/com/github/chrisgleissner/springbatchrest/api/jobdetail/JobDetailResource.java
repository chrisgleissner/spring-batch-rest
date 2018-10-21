package com.github.chrisgleissner.springbatchrest.api.jobdetail;

import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class JobDetailResource extends ResourceSupport {
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
