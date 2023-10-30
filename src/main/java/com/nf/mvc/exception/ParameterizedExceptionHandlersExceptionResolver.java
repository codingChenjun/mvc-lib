package com.nf.mvc.exception;

import com.nf.mvc.MethodArgumentResolver;
import com.nf.mvc.argument.MethodArgumentResolverComposite;
import com.nf.mvc.argument.MethodParameter;
import com.nf.mvc.handler.HandlerMethod;
import com.nf.mvc.support.MethodInvoker;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 此异常解析器让异常处理方法能支持的参数除了异常以外,还支持handler方法所能使用的的参数类型.
 * 此异常解析器需要与{@link  ExceptionHandlers}注解配合使用
 * <h3>典型用法</h3>
 * <pre class="code">
 *    &#064;ExceptionHandlers({AException.class,BException.class})
 *    public JsonViewResult handleArithmeticException(String a,AException re){
 *        System.out.println("异常处理方法参数a = " + a);
 *        return new JsonViewResult(new ResponseVO(10002,"异常消息:" + re.getMessage(),"a异常--"));
 *    }
 * </pre>
 * <h3>功能限制</h3>
 * 这里在解析参数时没有考虑HttpServletRequest已经被解析读取过的情况,意思就是假定已经在handler的方法参数解析过程中已经读取了输入流
 * 并关闭了(比如在反序列化解析时),这里仍然需要读取流的话就会抛出异常,框架目前是不支持这种情况的
 */
public class ParameterizedExceptionHandlersExceptionResolver extends ExceptionHandlersExceptionResolver {
  @Override
  protected Object executeExceptionHandlerMethod(HandlerMethod exceptionHandlerMethod, Exception exposedException, HttpServletRequest request) throws Exception{
    MethodArgumentResolverComposite argumentResolver = MethodArgumentResolverComposite.defaultInstance();
    // 这里要用insert相关方法添加,用add相关方法添加就会用BeanPropertyMethodArgumentResolver解析异常参数了
    argumentResolver.insertResolvers(new ExceptionArgumentResolver(exposedException));

    MethodInvoker methodInvoker = new MethodInvoker(argumentResolver);
    Object instance = exceptionHandlerMethod.getHandlerObject();
    Method method = exceptionHandlerMethod.getMethod();

    return methodInvoker.invoke(instance, method, request);
  }

  private static class ExceptionArgumentResolver implements MethodArgumentResolver{
    private final Exception raisedException;

    public ExceptionArgumentResolver(Exception raisedException) {
      this.raisedException = raisedException;
    }

    @Override
    public boolean supports(MethodParameter parameter) {
      return Exception.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request) throws Exception {
      return this.raisedException;
    }
  }
}
