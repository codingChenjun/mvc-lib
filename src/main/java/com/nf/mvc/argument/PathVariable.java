package com.nf.mvc.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解修饰在方法参数上,用来从请求路径上获取数据赋值给参数,
 * 不支持默认值,因为当前请求url只有匹配了路径模式,handler方法才会得到执行,
 * 也就意味着路径变量要么有值,要么handler方法不会得到执行.
 * <p>注解的value属性指定的路径变量的名字,类似下面用法
 * <pre class="code">
 *    &#064;RequestMapping("/list/{no}/{size}")
 *     public JsonViewResult simple(@PathVariable("no") int pageNo){}
 * </pre>
 * </p>
 * <p>参数的类型必须是简单类型,复杂类型没什么意义,数据也可能产生转换异常的问题</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathVariable {
    String value() ;
}
