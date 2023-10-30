package com.nf.mvc.util;

import javax.servlet.http.HttpServletRequest;

/**
 * <h3>参考资料</h3>
 * <a href="https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl">getRequestUri,getPathInfo等方法区别</a>
 * <a href="https://codebox.net/pages/java-servlet-url-parts">request对象路径相关方法含义</a>
 */
public abstract class RequestUtils {
    /**
     * 用来获取当前请求地址，排除掉上下文（contextPath）的部分,剩下的部分就当做请求地址交给{@link com.nf.mvc.HandlerMapping}去处理
     * <p>这种实现比较简单,不是非常完整科学,比如DispatcherServlet的模式为/test/*,而当前请求地址为/test/abc,
     * 那么请求地址应该是/abc,{@link  com.nf.mvc.HandlerMapping}应该处理的地址也应该是/abc</p>
     * <p>如果你想实现这样的效果,可以直接把spring-web依赖中,UrlPathHelper类中的方法getLookupPathForRequest或getPathWithinServletMapping源码复制到这里来</p>
     * @param request 当前请求
     * @return 请求地址,此地址会交给HandlerMapping处理
     */
    public static String getRequestUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return request.getRequestURI().substring(contextPath.length());
    }
}
