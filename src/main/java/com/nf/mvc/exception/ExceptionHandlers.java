package com.nf.mvc.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用在{@link ExceptionHandlersExceptionResolver}与{@link ParameterizedExceptionHandlersExceptionResolver}里,
 * 用来给异常处理方法指定能处理的多个异常类型
 *
 * @see ExceptionHandlersExceptionResolver
 * @see ParameterizedExceptionHandlersExceptionResolver
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandlers {
    Class<? extends Exception>[] value() ;
}
