package com.github.chrisgleissner.springbatchrest.util.adhoc;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class JobConfig {
    private String name;
    @Singular("property")
    private Map<String, String> properties = new HashMap<>();
    private boolean asynchronous;
}
