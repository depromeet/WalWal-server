package com.depromeet.stonebed.global.annotation;

import com.depromeet.stonebed.global.common.constants.EnvironmentConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional({OnProfileCondition.class})
public @interface ConditionalOnProfile {
    EnvironmentConstants[] value() default {EnvironmentConstants.LOCAL};
}
