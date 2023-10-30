package com.nf.mvc.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本注解只用在字段上，并且当前此类只是应用注入配置属性类的注入这一种使用场景
 * 具体的实现见{@link com.nf.mvc.util.ReflectionUtils#injectConfigurationProperties(Object)}
 * @see com.nf.mvc.util.ReflectionUtils
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Injected {
}
