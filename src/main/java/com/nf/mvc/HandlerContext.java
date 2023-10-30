package com.nf.mvc;

import com.nf.mvc.argument.MethodParameter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 此类是一个线程安全的上下文性质的类，用来保存每次处理请求时的request与response对象，
 * 以便在请求处理周期的任何地方都可以随时获取到这些对象，方便进行一些处理，可以一定程度上弥补某些接口或者控制器参数过多的问题
 *
 * <p>
 *     设计这个类出来主要是演示在多线程执行情况下，不同请求有不同request与response怎么保存的问题。
 *     此类主要是通过ThreadLocal来实现，让每一个请求线程都保存一份线程独有的HandlerContext，进而也就保存了request，response等对象
 *     存储在ThreadLocal中的对象一定要记得删除，以防止内存泄漏的问题，这点见{@link DispatcherServlet#doService(HttpServletRequest, HttpServletResponse)}
 *     finally代码块
 * </p>
 *
 * <p>此类除了演示多线程情况下，怎么保存独属于当前线程的对象这种技术外，此类还是有必要存在的，比如在Servlet API
 * 参数解析器中获取response对象，见{@link com.nf.mvc.argument.ServletApiMethodArgumentResolver#resolveArgument(MethodParameter, HttpServletRequest)}</p>
 * <p>
 *     框架内使用方式如下：
 *     <pre class="code">
 *          HandlerContext().getContext().setRequest(req).setResponse(resp);
 *     </pre>
 *     这些set方法修饰符是默认的，基本是给框架内部使用，用户不应该调用这些方法
 * </p>
 *
 * <p>
 *     用户主要是靠此类来获取保存的request，response等对象，使用方式如下
 *     <pre class="code">
 *          HandlerContext().getContext().getXxx()
 *     </pre>
 * </p>
 * @see DispatcherServlet
 */
public class HandlerContext {

    private static final ThreadLocal<HandlerContext> local = new ThreadLocal<>();
    private HttpServletRequest request;
    private HttpServletResponse response;

    public static HandlerContext getContext(){
        //等于null表示当前线程没有一个本地的HandlerContext对象
        if (local.get() == null) {
            HandlerContext context = new HandlerContext();
            local.set(context);
        }
        return local.get();
    }

    HandlerContext setRequest(HttpServletRequest request) {
        this.request = request;
        return  this;
    }

    HandlerContext setResponse(HttpServletResponse response) {
        this.response = response;
        return  this;
    }

    public HttpServletRequest getRequest(){
        return request;
    }

    public HttpServletResponse getResponse(){
        return response;
    }

    public HttpSession getSession(){
        return request.getSession();
    }

    public ServletContext getApplication(){
        return request.getServletContext();
    }

    public void clear(){
        local.remove();
    }
}
