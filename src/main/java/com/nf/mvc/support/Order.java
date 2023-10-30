package com.nf.mvc.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解是给4大类mvc框架扩展用组件来决定顺序用的，只会在同类型的组件上考虑此注解的值，
 * 值越大表示优先级越低，不指定value的值，默认情况下是最低优先级
 * <p>
 *  自定义组件可以不加注解，不加注解等于其Order值是整数最大值
 *  如果你加了Order注解，就以注解的指定的值为准,如果都不加注解，就是以扫描的顺序为准
 * </p>
 *
 * <p>
 *   定制组件的优先级总是高过同类型的mvc框架提供的默认组件
 * </p>
 * @see OrderComparator
 * @see com.nf.mvc.MvcContext
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    //默认值是整数的最大值，就表示优先级最低（1->2->3)
    int value() default Integer.MAX_VALUE;
}
