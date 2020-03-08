package com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.github.chrisgleissner.springbatchrest.api.core.Constants.REST_API_ENABLED;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ConditionalOnProperty(name = REST_API_ENABLED, havingValue = "true", matchIfMissing = true)
@RestController
@RequestMapping(value = "/jobDetails", produces = "application/hal+json")
public class JobDetailController {

    @Autowired
    private JobDetailService jobDetailService;

    @Operation(summary = "Get a Quartz job detail by Quartz group and job name")
    @GetMapping("/{quartzGroupName}/{quartzJobName}")
    public JobDetailResource get(@PathVariable String quartzGroupName, @PathVariable String quartzJobName) {
        return new JobDetailResource(jobDetailService.jobDetail(quartzGroupName, quartzJobName));
    }

    @Operation(summary = "Get all Quartz job details")
    @GetMapping
    public CollectionModel<JobDetailResource> all(@RequestParam(value = "enabled", required = false) Boolean enabled,
                                            @RequestParam(value = "springBatchJobName", required = false) String springBatchJobName) {
        return new CollectionModel<>(jobDetailService.all(Optional.ofNullable(enabled), Optional.ofNullable(springBatchJobName)).stream()
                .map(JobDetailResource::new).collect(toList()),
                WebMvcLinkBuilder.linkTo(methodOn(JobDetailController.class).all(enabled, springBatchJobName)).withSelfRel().expand());
    }
}
