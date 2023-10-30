package com.nf.mvc;

import com.nf.mvc.support.path.AntPathMatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解修饰在拦截器上，用来指定要拦截的地址与不进行拦截的地址，
 * 默认的HandlerMapping实现类{@link com.nf.mvc.mapping.RequestMappingHandlerMapping}
 * 对于拦截器地址的解析是采用ant地址模式进行解析的
 *
 * <p>通过value属性指定要拦截的地址，默认值是拦截所有的地址<br/>
 * 通过excludePattern属性指定不拦截的地址，默认值是空，意思就是没有要排除的地址
 * </p>
 *
 * <p>如果includePattern与excludePattern设置有冲突以排除设置为准</p>
 * @see com.nf.mvc.HandlerInterceptor
 * @see AntPathMatcher
 * @see com.nf.mvc.mapping.RequestMappingHandlerMapping
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Intercepts {
    String[] value() default {"/**"};
    String[] excludePattern() default {""};
}
