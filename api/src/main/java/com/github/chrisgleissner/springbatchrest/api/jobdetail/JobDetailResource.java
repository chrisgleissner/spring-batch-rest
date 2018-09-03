package com.github.chrisgleissner.springbatchrest.api.jobdetail;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class JobDetailResource extends ResourceSupport {
    private final JobDetail jobDetail;

    public JobDetailResource(final JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        add(linkTo(methodOn(JobDetailController.class).get(jobDetail.getQuartzGroupName(), jobDetail.getQuartzJobName())).withSelfRel());
    }
}
