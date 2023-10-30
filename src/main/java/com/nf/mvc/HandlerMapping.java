package com.nf.mvc;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 此接口用来依据请求找到对应的处理者（Handler)<br/>
 *
 * <p>
 * 此接口的实现类通常需要具备三个能力:
 *     <ul>
 *         <li>内部有一个容器，用来保存其所有能处理请求的Handler</li>
 *         <li>依据当前请求，找到对应的Handler</li>
 *         <li>依据当前请求，找到此请求有关的拦截器</li>
 *     </ul>
 * </p>
 * @see com.nf.mvc.mapping.RequestMappingHandlerMapping
 * @see HandlerExecutionChain
 * @see HandlerInterceptor
 * @see Intercepts
 * @see DispatcherServlet
 */
public interface HandlerMapping {
    /**
     * 通常会依据当前请求的相关信息，比如requestURI信息来获取处理者，如果返回null表示本HandlerMapping不能处理此请求，
     * 交给下一个HandlerMapping去处理
     * @param request：当前请求
     * @return 请求处理的执行链
     * @throws Exception 获取Handler过程中抛出的异常
     */
    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

    /**
     * 默认的拦截器实现，这种实现是把所有的拦截器都应用到当前请求中去，并不考虑请求地址与注解Interceptors的情况
     * @param request servlet请求对象
     * @return 所有当前请求的拦截器
     */
    default List<HandlerInterceptor> getInterceptors(HttpServletRequest request){
        return MvcContext.getMvcContext().getCustomHandlerInterceptors();
    }
}
