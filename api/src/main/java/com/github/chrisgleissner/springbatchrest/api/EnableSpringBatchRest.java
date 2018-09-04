package com.github.chrisgleissner.springbatchrest.api;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables Spring Batch REST endpoints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SpringBatchRestAutoConfiguration.class)
public @interface EnableSpringBatchRest {
}
