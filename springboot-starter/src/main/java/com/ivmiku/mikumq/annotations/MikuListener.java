package com.ivmiku.mikumq.annotations;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MikuListener {
    String[] queuesToDeclare() default {};
    String tag() default "";
}
