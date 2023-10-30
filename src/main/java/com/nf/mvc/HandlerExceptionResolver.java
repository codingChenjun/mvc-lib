package com.nf.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此异常解析器是处理Handler执行过程中产生的异常的，也就是说是执行链产生的异常才会处理，
 * 视图渲染层面的异常是没有处理的，要处理的话可以在{@link DispatcherServlet#doService(HttpServletRequest, HttpServletResponse)}方法里处理
 * <p>
 *  编写自己的异常解析时，如果异常不是你能处理的，就尽快返回null结束处理,比如:
 *  <pre class="code">
 *     public class MyHandlerExceptionResolver implements HandlerExceptionResolver{
 *         ViewResult resolveException(
 *             HttpServletRequest request, HttpServletResponse response,  Object handler, Exception ex){
 *                 if(!(ex instanceOf RuntimeException)){
 *                     return null;
 *                 }
 *                 //下面才是真正的解析逻辑
 *             }
 *     }
 *  </pre>
 *  上面的示例代码可以参考:{@link com.nf.mvc.exception.LogHandlerExceptionResolver}
 * </p>
 *
 * <p>
 *     {@link com.nf.mvc.exception.LogHandlerExceptionResolver}与{@link com.nf.mvc.exception.PrintStackTraceHandlerExceptionResolver}
 *     这两个类更多的是起到演示效果，实际用处不大，主要是{@link com.nf.mvc.exception.ExceptionHandlerExceptionResolver}来处理执行链的异常处理逻辑
 * </p>
 * @see DispatcherServlet
 * @see com.nf.mvc.exception.ExceptionHandler
 * @see com.nf.mvc.exception.ExceptionHandlerExceptionResolver
 * @see com.nf.mvc.exception.LogHandlerExceptionResolver
 * @see com.nf.mvc.exception.PrintStackTraceHandlerExceptionResolver
 */
public interface HandlerExceptionResolver {
    /**
     * 执行链的异常解析器设计为只处理Exception异常，Error异常是不处理的，
     * <p>异常解析器能解析就正常返回一个ViewResult类型的对象，
     * 异常解析器解析过程中出异常或者返回null被认为是解析不了当前异常，
     * 框架会查找下一个异常解析器去解析异常</p>
     * 由{@link DispatcherServlet#doService(HttpServletRequest, HttpServletResponse)}去处理
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理者，通常是控制器方法所在类的对象
     * @param ex 控制器方法执行时抛出的异常
     * @return 返回解析之后的ViewResult
     * @see com.nf.mvc.exception.ExceptionHandlerExceptionResolver
     */
    ViewResult resolveException(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
}
