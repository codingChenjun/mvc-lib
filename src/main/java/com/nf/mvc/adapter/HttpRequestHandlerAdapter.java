package com.nf.mvc.adapter;

import com.nf.mvc.HandlerAdapter;
import com.nf.mvc.HttpRequestHandler;
import com.nf.mvc.ViewResult;
import com.nf.mvc.handler.HandlerClass;
import com.nf.mvc.view.VoidViewResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此HandlerAdapter是用来处理实现了{@link HttpRequestHandler}接口的处理者,
 * <p>此接口的方法{@link HttpRequestHandler#processRequest(HttpServletRequest, HttpServletResponse)}设计为返回void，
 * 所以此接口的实现类如果要响应请求，只能通过方法的参数HttpServletResponse对象直接生成响应，此HandlerAdapter会把Handler的结果适配为VoidViewResult</p>
 * @see HttpRequestHandler
 * @see VoidViewResult
 * @see HandlerAdapter
 */
public class HttpRequestHandlerAdapter implements HandlerAdapter {
    @Override
    public boolean supports(Object handler) {
       return handler instanceof HandlerClass
                && HttpRequestHandler.class
                        .isAssignableFrom(((HandlerClass)handler).getHandlerClass());
    }

    @Override
    public ViewResult handle(HttpServletRequest req,
                             HttpServletResponse resp,
                             Object handler) throws Exception {
        HandlerClass handlerClass = (HandlerClass) handler;
        Object instance = handlerClass.getHandlerObject();

        HttpRequestHandler requestHandler = (HttpRequestHandler) instance;
        requestHandler.processRequest(req, resp);
        return new VoidViewResult();

    }
}
