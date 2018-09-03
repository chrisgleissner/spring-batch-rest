package com.github.chrisgleissner.springbatchrest.api.job;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.hateoas.ResourceSupport;

@Value
@AllArgsConstructor
public class Job extends ResourceSupport {
    private String name;
}
