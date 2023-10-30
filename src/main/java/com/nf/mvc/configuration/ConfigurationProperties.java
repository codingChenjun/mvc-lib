package com.nf.mvc.configuration;

import com.nf.mvc.HandlerAdapter;
import com.nf.mvc.HandlerMapping;
import com.nf.mvc.MvcContext;
import com.nf.mvc.argument.RequestBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解修饰在配置属性类上，用来把yml文件中的内容赋值给配置属性类的相关属性上,
 * <h3>赋值规则</h3>
 * <ul>
 *     <li>一定要靠注解指定前缀,只会读取yml中对应前缀下的内容</li>
 *     <li>如果本类的属性名在配置文件中有对应项，就进行赋值处理</li>
 * </ul>
 * <h3>能使用配置属性的类型</h3>
 * <ul>
 *   <li>一定是被mvc管理的,也就是其实例是mvc框架创建出来的</li>
 *   <li>目前主要是用户编写的控制器与控制器使用的复杂类型参数,
 *   {@link RequestBody}注解修饰的类型也不能注入</li>
 *   <li>用户编写的扩展mvc框架用的类型,比如{@link HandlerMapping},{@link HandlerAdapter}等类型</li>
 * </ul>
 * 配置属性类会在初始化阶段创建出来,其实例是单例的,可以通过{@link MvcContext#getConfigurationProperties()}来获取,
 * 利用这个特性,你就可以把配置属性类应用在上面列出的三大类地方的任意其他地方了
 * @author cj
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationProperties {
    /**
     * 指定在yml配置文件前缀用的
     * @return 前缀
     */
    String value();
}
