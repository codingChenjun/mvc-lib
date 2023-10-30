package com.nf.mvc.argument;

import com.nf.mvc.HandlerContext;
import com.nf.mvc.MethodArgumentResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.nf.mvc.HandlerContext.getContext;

/**
 * 此解析器主要是用来解析常见的Servlet相关的类型，比如HttpServletRequest，HttpSession等
 * @see HandlerContext
 */
public class ServletApiMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supports(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return Arrays.stream(ServletApiEnum.values()).anyMatch(api->api.getSupportedClass().isAssignableFrom(paramType));
    }

    /**
     * 参数解析，是从请求中获取数据的，所以方法设计没有response对象是合理的
     * 但这样给我们带来一个解析的问题，无法获取到response对象
     * request与response对象必须来自于DispatcherServlet的service
     * @param parameter MethodParameter
     * @param request 请求对象
     * @return 解析之后的值
     * @throws Exception 解析过程中可能抛出的异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request) throws Exception {
        Class<?> paramType = parameter.getParameterType();
        // supports方法返回true才会调用resolveArgument方法，所以ServletApiEnum.of(paramType)不会返回null
        return ServletApiEnum.of(paramType).getValue();
    }

    /**
     * 枚举项的名字必须是支持类型的简单名字，这样才能使用ServletApiEnum.valueOf(paramType.getSimpleName())
     * 来获取枚举实例，否则你就只能用{@link #of(Class)}来获取枚举，这样就不要求枚举项是支持类型的简单名
     *
     * <p>枚举作为内部类加static关键字是多余的</p>
     * <p><b>注意：这个类设计出来主要是为了演示通过enum来优化if过多的技巧</b></p>
     */
    private enum ServletApiEnum{
        HttpServletRequest(HttpServletRequest.class,()->getContext().getRequest()),
        HttpServletResponse(HttpServletResponse.class,()-> getContext().getResponse()),
        HttpSession(HttpSession.class,()-> getContext().getSession()),
        ServletContext(ServletContext.class,()-> getContext().getApplication());

        private final Class<?> supportedClass;
        private final Supplier<Object> valueSupplier;

        ServletApiEnum(Class<?> supportedClass, Supplier<Object> valueSupplier) {
            this.supportedClass = supportedClass;
            this.valueSupplier = valueSupplier;
        }

        public Class<?> getSupportedClass() {
            return supportedClass;
        }

        public Object getValue(){
            return this.valueSupplier.get();
        }

        /**
         * 此方法依据类型来返回对应的枚举实例的，枚举类自带的valueOf方法是一种严格相等的形式
         * 来返回对应的枚举实例，这里是看apiClass是不是可以赋值给对应枚举实例支持的类型
         * @param apiClass api类型
         * @return 返回对应的枚举实例
         */
        public static ServletApiEnum of(Class<?> apiClass) {
            ServletApiEnum[] values = ServletApiEnum.values();
            for (ServletApiEnum value : values) {
                if (value.getSupportedClass().isAssignableFrom(apiClass)) {
                    return value;
                }
            }
            return null;
        }
    }
}
