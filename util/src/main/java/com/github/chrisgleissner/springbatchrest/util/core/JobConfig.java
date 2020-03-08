package com.github.chrisgleissner.springbatchrest.util.core;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class JobConfig {
    private String name;
    @Singular("property")
    private Map<String, Object> properties = new HashMap<>();
    private boolean asynchronous;
}
