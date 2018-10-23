package com.github.chrisgleissner.springbatchrest.api.annotations;

import com.github.chrisgleissner.springbatchrest.api.configuration.SpringBatchRestConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({SpringBatchRestConfiguration.class})
public @interface EnableSpringBatchRest {
}
