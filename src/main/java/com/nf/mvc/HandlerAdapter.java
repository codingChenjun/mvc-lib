package com.nf.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此类的作用就是让HandlerMapping找到的某个handler得到执行，以处理请求
 * <p>
 *     由于mvc框架并没有对Handler是什么进行任何的限制，只有java语言对handler进行了限制，
 *     那就是只能是属于某个类的一个方法，方法的签名与所在的类并不做任何的限制。
 *     基于这种情况，handler执行mvc框架是不可能知道，所以只能交给HandlerAdapter去处理，如果你创造了一个新的Handler，
 *     只要能创造一个知道怎么执行Handler的HandlerAdapter即可。
 * </p>
 * <p>
 *     DispatcherServlet会遍历所有的HandlerAdapter，调用其{@link #supports(Object)}方法，只要返回true
 *     就结束遍历，表明有一个HandlerAdapter知道怎么处理当前的Handler，如果没有Adapter可以处理，DispatcherServlet
 *     会抛出异常，具体的实现逻辑见{@link DispatcherServlet#getHandlerAdapter(Object)}.
 *     需要注意的是：HandlerAdapter只负责Handler的执行，不负责拦截器链的执行。
 * </p>
 * <p>
 *     handler方法为了灵活，可以返回以下类型：
 * <ul>
 *     <li>可以返回void，这种情况一般是handler方法直接利用response对象响应请求</li>
 *     <li>返回{@link ViewResult}</li>
 *     <li>返回其它类型，这种情况比较少见(不建议)，框架直接把其适配为返回{@link com.nf.mvc.view.PlainViewResult}</li>
 * </ul>
 * 注意：通过反射调用一个返回类型为void的方法时，其返回值是null的。所以，一个Handler方法反射调用返回null，可能是void返回类型或者本身return null，
 * 具体的Handler执行结果适配逻辑见{@link ViewResult#adaptHandlerResult(Object)}
 * </p>
 * @author cj
 * @see com.nf.mvc.view.PlainViewResult
 * @see com.nf.mvc.ViewResult
 * @see com.nf.mvc.adapter.RequestMappingHandlerAdapter
 * @see DispatcherServlet
 */
public interface HandlerAdapter {
    boolean supports(Object handler);

    ViewResult handle(HttpServletRequest req, HttpServletResponse resp,Object handler) throws Exception;
}
