package com.github.chrisgleissner.springbatchrest.api;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({SpringBatchRestConfiguration.class})
public @interface EnableSpringBatchRest {
}
