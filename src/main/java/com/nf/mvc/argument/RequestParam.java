package com.nf.mvc.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    String value() default ValueConstants.DEFAULT_NONE;
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
