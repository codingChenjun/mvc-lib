package com.nf.mvc.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nf.mvc.ViewResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nf.mvc.util.JacksonUtils.getObjectMapper;


public class JsonViewResult extends ViewResult {
    private final Object obj;

    public JsonViewResult(Object obj) {
        this.obj = obj;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        ObjectMapper objectMapper = getObjectMapper();
        String jsonText = objectMapper.writeValueAsString(this.obj);
        resp.getWriter().print(jsonText);
    }


}
