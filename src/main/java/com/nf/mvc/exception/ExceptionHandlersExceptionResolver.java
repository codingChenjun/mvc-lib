package com.nf.mvc.exception;

import com.nf.mvc.HandlerExceptionResolver;
import com.nf.mvc.handler.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 本异常解析器支持异常处理方法通过注解{@link ExceptionHandlers}指定能处理多个异常的情况，
 * 使用情况如下：
 * <pre class="code">
 *     &#64;ExceptionHandlers({AException.class,BException.class})
 *     public void handleExaAndExb(Exception ex){
 * <p>
 *     }
 * </pre>
 * <p><b>注意:异常处理方法的参数仍然只能有一个异常类型的参数，而且此异常参数必须是注解指定的异常或者是其父类型,
 * 通常情况下异常参数会是注解指定的各个异常的共同父类型,但此异常解析器并不强制要求这一点</b></p>
 *
 * <p>如果想让异常处理方法除了支持异常类型作为参数,还支持类似于控制器方法的参数类型,
 * 那么就需要使用{@link ParameterizedExceptionHandlersExceptionResolver}</p>
 *
 * <p>此类并没有直接使用在框架中,而是用了其子类{@link ParameterizedExceptionHandlersExceptionResolver}</p>
 * @see com.nf.mvc.DispatcherServlet
 * @see HandlerExceptionResolver
 * @see ExceptionHandlers
 * @see ExceptionHandlerExceptionResolver
 * @see ParameterizedExceptionHandlersExceptionResolver
 */
public class ExceptionHandlersExceptionResolver extends ExceptionHandlerExceptionResolver {

    /**
     * 这样重写之后，相当于只有scan功能，没有添加后置处理的功能，等价于把方法{@link #postHandleExceptionHandlerMethods(List)}
     * 重写为一个空的方法
     */
    @Override
    protected void resolveExceptionHandlerMethods() {
        scanExceptionHandlerMethods(method -> method.isAnnotationPresent(ExceptionHandlers.class));
    }

    @Override
    protected HandlerMethod findMostMatchedHandlerMethod(List<HandlerMethod> handlerMethods,Exception exception) {
        HandlerMethod mostMatchedHandlerMethod = null;
        Class<?> mostMatchedExceptionClass = null;
        for (HandlerMethod exHandleMethod : handlerMethods) {
            Method method = exHandleMethod.getMethod();
            Class<? extends Exception>[] exceptionClasses = method.getDeclaredAnnotation(ExceptionHandlers.class).value();
            Class<?> matchedExceptionClass;
            for (Class<? extends Exception> exceptionClass : exceptionClasses) {
                if (exceptionClass.isAssignableFrom(exception.getClass())) {
                    matchedExceptionClass = exceptionClass;
                    if (mostMatchedExceptionClass == null) {
                        mostMatchedExceptionClass = matchedExceptionClass;
                    }
                    if (mostMatchedExceptionClass.isAssignableFrom(matchedExceptionClass)) {
                        mostMatchedExceptionClass = matchedExceptionClass;
                        mostMatchedHandlerMethod = exHandleMethod;
                    }
                }
            }
        }
        return mostMatchedHandlerMethod;
    }
}
