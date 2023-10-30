package com.nf.mvc.argument;

import com.nf.mvc.MethodArgumentResolver;
import com.nf.mvc.MvcContext;
import com.nf.mvc.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 *这是一个解析参数类型为自定义的bean类型的参数解析器，此解析器只处理bean的属性（setter方法），
 * 如果属性的类型是非bean类型，那么交给其它的参数解析器去解析，如果仍然是bean类型，就进行递归解析处理<br/>
 *
 * <i>这个解析器要求是参数解析器链中的最后一个解析器。</i>
 * <h3>解析说明</h3>
 * <p>此解析器解析通常编写的pojo bean类型，也支持嵌套bean的解析，
 * 数据源的key是属性名，嵌套bean的话属性名用句号分隔，类似于el表达式的写法</p>
 * 假定有下面这样一个控制器方法，其参数是POJO类Emp，那么request对象的map中如果有对应的key就可以把此Emp解析出来
 * <pre class="code">
 *     public class SomeController{
 *         public void insert(Emp emp){}
 *     }
 *
 *     &#064;Data
 *     public class Emp{
 *         private int id;
 *         private String name;
 *         private Dept dept;
 *     }
 *
 *     &#064;Data
 *     public class Dept{
 *         private int deptId;
 *         private String deptName;
 *         private Manager manager;
 *     }
 *
 *     &#064;Data
 *     public class Manager{
 *         private String title;
 *     }
 *
 *     private Map<String,String> requestMap = new HashMap<>() ;
 *     requestMap.put("id", "100");
 *     requestMap.put("name", "abc");
 *     requestMap.put("dept.deptId", "1111");
 *     requestMap.put("dept.deptName", "nested hr");
 *     requestMap.put("dept.manager.title", "jingli");
 *     requestMap.put("dept.manager.youxi.gameName", "starcraft");
 *
 * </pre>
 * </p>
 * <h3>实现逻辑</h3>
 * <ol>
 *     <li>其它解析器解析不了就认为此类是一个复杂类型,创建此类的实例</li>
 *     <li>获取并遍历其所有settter方法并调用</li>
 *     <li>如果setter方法的参数其它解析器可以解析就交给其它解析器解析</li>
 *     <li>如果setter方法的参数其它解析器解析不了，就重复第一步，递归处理</li>
 * </ol>
 * <h3>自定义解析链</h3>
 * <p>此类利用了{@link MethodArgumentResolverComposite}类进行了自定义的解析器组合，
 * 先利用这个解析器组合进行解析，解析不了就交给本类解析</p>
 * <p>
 *     需要注意的是BeanProperty解析器本身是单例的，频繁创建自定义的解析器组合，明显并不是一个好主意，这里通过创建一次后，
 *     后续直接返回解析器组合的方式避免重复创建的问题，但由于参数解析器是运行在多线程的环境下，所以为了线程安全性，
 *     这里采用双重检查+volatile的形式解决这一问题，具体见{@link #getResolvers()}方法
 * </p>
 *
 * @see MethodArgumentResolverComposite
 * @see MethodArgumentResolver
 */
public class BeanMethodArgumentResolver implements MethodArgumentResolver {

    private volatile MethodArgumentResolverComposite resolvers = null;

    @Override
    public boolean supports(MethodParameter parameter) {
        return !getResolvers().supports(parameter);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request) throws Exception {
        // 进到这里来是因为supports为true，也就是其它解析器都无法解析了参数了，
        // 所以直接实例化参数类型的实例，接着调用所有的setter方法以填充bean
        Stack<String> prefixStack = new Stack<>();
        Object bean = ReflectionUtils.newInstance(parameter.getParameterType());
        populateBean(bean, request, prefixStack);
        return bean;
    }

    /**
     * 这个方法是用来对原始的method中复杂类型参数的属性进行值的填充操作的（比如示例代码中Emp类型）
     * @param instance：要填充的bean实例
     * @param request:数据来源
     * @param prefixStack:用来存放属性名前缀的
     * @throws Exception 填充bean时可能抛出的异常
     */
    private void populateBean(Object instance, HttpServletRequest request, Stack<String> prefixStack) throws Exception {
        List<Method> allSetterMethods = ReflectionUtils.getAllSetterMethods(instance.getClass());

        for (Method setterMethod : allSetterMethods) {
            String parameterName = ReflectionUtils.getParameterNames(setterMethod).get(0);
            MethodParameter setterMethodParameter = new MethodParameter(setterMethod, 0, parameterName);

            if (!getResolvers().supports(setterMethodParameter)) {
                // 前缀就是setter方法的名字，比如setDept方法，那么前缀就是dept
                String prefix = setterMethod.getName().substring(3, 4).toLowerCase() + setterMethod.getName().substring(4);
                prefixStack.push(prefix);
                invokeSetterMethod(instance, setterMethod, parameterName, request, prefixStack);
                prefixStack.pop();
            } else {
                invokeSetterMethod(instance, setterMethod, parameterName, request, prefixStack);
            }
        }
    }

    /**
     * 调用setter方法，相当于在填充bean实例，所有的setter方法都得到调用，就表示bean实例填充完毕
     * @param instance:setter方法所在类的实例
     * @param method:某一个setter方法,setter方法有且只有一个参数
     * @param request:数据来源
     * @param parameterName:当前setter方法的唯一的一个参数的名字
     * @param prefixStack:属性名前缀栈
     * @throws Exception 反射调用bean的setter方法时可能抛出的异常
     */
    private void invokeSetterMethod(Object instance, Method method, String
            parameterName,HttpServletRequest request, Stack<String> prefixStack) throws Exception {
        // 如果需要加前缀的话(prefixStack不为空)，就把参数名添加上前缀
        parameterName = handleParameterName(prefixStack, parameterName);

        Object[] paramValues = new Object[1];
        MethodParameter methodParameter = new MethodParameter(method, 0, parameterName);
        paramValues[0] = resolveSetterArgument(methodParameter, request, prefixStack);

        method.invoke(instance, paramValues);
    }

    /**
     * 处理属性名，这个属性就是用来从request对象中提前数据的key值:req.getParameter(key)
     * @param prefixStack: 前缀栈
     * @param parameterName：setter方法的参数名
     * @return: 返回的是添加了前缀的参数名
     */
    private String handleParameterName(Stack<String> prefixStack, String parameterName) {
        if (prefixStack.isEmpty()) {
            return parameterName;
        }

        StringBuilder prefix = new StringBuilder();
        for (String s : prefixStack) {
            prefix.append(s).append(".");
        }

        parameterName  = prefix + parameterName;
        return parameterName;
    }

    private Object resolveSetterArgument(MethodParameter parameter, HttpServletRequest request, Stack<String> prefixStack) throws Exception {
        if (getResolvers().supports(parameter)) {
            return getResolvers().resolveArgument(parameter, request);
        } else {
            Object bean = ReflectionUtils.newInstance(parameter.getParameterType());
            populateBean(bean, request, prefixStack);
            return bean;
        }
    }

    /**
     * 此复杂类型的解析器利用其它的解析器来进行数据解析，所以要排除掉自己
     * <p>参数解析器是单例的，但其运行在多线程环境下，
     * 下面的代码采用的是双重检查的方式确保resolvers只会被求值一次以确保线程安全性</p>
     * @return 返回框架中除了自己以外的其它所有参数解析器
     */
    private MethodArgumentResolverComposite getResolvers() {
        if (resolvers == null) {
            synchronized (BeanMethodArgumentResolver.class) {
                if(resolvers == null) {
                    resolvers = new MethodArgumentResolverComposite().addResolvers(
                            MvcContext.getMvcContext().getArgumentResolvers().stream()
                                    .filter(r -> !(r instanceof BeanMethodArgumentResolver))
                                    .collect(Collectors.toList()));
                }
            }
        }

        return resolvers;
    }
}
