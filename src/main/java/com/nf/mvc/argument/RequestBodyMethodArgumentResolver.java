package com.nf.mvc.argument;

import com.nf.mvc.MethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;

import static com.nf.mvc.util.JacksonUtils.fromJson;

/**
 * 此解析器只解析参数上有注解{@link RequestBody}修饰的参数
 * <p>注意：此解析器是利用请求的输入流进行反序列化解析的，反序列化之后流就关闭了，
 * 所以，不支持在方法有多个注解{@link RequestBody}修饰的参数,比如下面的写法：
 * <pre class="code">
 *   &#064;RequestMapping("/json")
 *   public JsonViewResult json(@RequestBody Emp emp,@RequestBody List<Emp> empList){}
 * </pre>
 * </p>
 * @see RequestBody
 * @see MethodParameter#isParameterizedType()
 */
public class RequestBodyMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supports(MethodParameter parameter) {
        return parameter.isPresent(RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request) throws Exception {
        return parameter.isParameterizedType() ?
                fromJson(request.getInputStream(), parameter.getParameterType(), parameter.getActualArguments())
                : fromJson(request.getInputStream(), parameter.getParameterType());
    }
}
