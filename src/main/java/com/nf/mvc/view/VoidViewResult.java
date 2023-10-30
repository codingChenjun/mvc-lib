package com.nf.mvc.view;

import com.nf.mvc.ViewResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VoidViewResult extends ViewResult {
    /**
     * 这个方法是一个空方法体，其应用的场景就是控制器方法已经利用HttpServletResponse
     * 对象进行了响应处理，用户或者mvc框架内部就可以利用这种类型的ViewResult来达成
     * HandlerAdapter执行了Handler之后一直的返回类型的效果
     * @param req 请求对象
     * @param resp 响应对象
     */
    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) {

    }
}
