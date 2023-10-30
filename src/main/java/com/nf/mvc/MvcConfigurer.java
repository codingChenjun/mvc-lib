package com.nf.mvc;

import com.nf.mvc.cors.CorsConfiguration;

/**
 * 此接口是用来给mvc框架内部的一些核心组件提供配置使用的,在整个mvc框架中，对mvc框架内部的组件有2种方式进行定制配置的
 * <ul>
 *     <li>继承DispatcherServlet，重写config开头的相关方法，这类方法的第一个参数是同类型的所有组件，这样你就有机会对所有的组件进行处理</li>
 *     <li>实现WebMvcConfigurer接口，一般是对某类组件的某一个具体的组件进行配置</li>
 * </ul>
 * <p>
 * 此接口采用默认方法而不是抽象方法，是因为用户不一定要对所有的组件进行配置，只需要重写特定的方法就可以对特定的组件进行配置了
 *
 * <p>
 * 实现类基本写法如下:
 * <pre class="code">
 *     public class MyWebMvcConfigurer implements WebMvcConfigurer{
 *        &#064;Override
 *        public void configureHandlerMapping(HandlerMapping handlerMapping){
 *              if(handlerMapping instanceOf 某个具体的HandlerMapping){
 *                  //对某个具体的HandlerMapping进行配置
 *              }
 *        }
 *     }
 *     </pre>
 * </p>
 *
 * <p>此接口由用户实现，一般只需要提供一个实现类即可，提供多个此接口的实现类是不允许的，会抛出异常</p>
 *
 * <p>这里的方法分别针对mvc框架内可以进行定制的组件进行定制配置的：
 * <ul>
 *     <li>configureHandlerMapping:对HandlerMapping进行配置</li>
 *     <li>configureHandlerAdapter：对HandlerAdapter进行配置</li>
 *     <li>configureArgumentResolver：对MethodArgumentResolver进行配置</li>
 *     <li>configureExceptionResolver：对HandlerExceptionResolver进行配置</li>
 *     <li>configureCors：对CorsConfiguration进行配置</li>
 * </ul>
 * </p>
 *
 * @see HandlerMapping
 * @see HandlerAdapter
 * @see MethodArgumentResolver
 * @see HandlerExceptionResolver
 * @see CorsConfiguration
 */
public interface MvcConfigurer {
    default void configureHandlerMapping(HandlerMapping handlerMapping) {

    }

    default void configureHandlerAdapter(HandlerAdapter adapter) {

    }

    default void configureArgumentResolver(MethodArgumentResolver argumentResolver) {

    }

    default void configureExceptionResolver(HandlerExceptionResolver exceptionResolver) {

    }

    default void configureCors(CorsConfiguration configuration) {
        configuration.applyDefaultConfiguration();
    }

}
