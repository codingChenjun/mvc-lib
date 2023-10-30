package com.nf.mvc.argument;

import com.nf.mvc.MethodArgumentResolver;
import com.nf.mvc.MvcContext;
import com.nf.mvc.support.WebTypeConverters;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * composite:复合，组合的意思，就是表示此类管理很多的解析器
 * <p>此类是一个典型的组合模式实现，自己本身是一个解析器，也管理着其它的一些解析器，类似于算术表达式4*5+7*8，
 * 其中4*5是一个完整的算术表达式，也是一个更大的算术表达式的一部分
 * </p>
 *
 * <p>
 * 构建解析器组合的使用方式如下：
 *     <ul>
 *         <li>直接实例化此类的一个对象，这样的对象不包含任何的解析器,然后通过调用addResolver与insertResolver的重载方法进行解析器组合，
 *         add开头的方法是在后面添加，insert开头的方法是在前面添加</li>
 *         <li>调用此类的静态方法{@link #defaultInstance()}方法来创建实例，此方法创建的实例包含所有的解析器</li>
 *         <li>有了此类的实例之后，调用addResolver之类的方法进行解析器的组合</li>
 *     </ul>
 *      典型的组合解析器的代码如下：
 *     <pre class="code">
 *         new HandlerMethodArgumentResolverComposite().addResolver(解析器1)
 *                      .addResolvers(解析器2，解析器3)...
 *     </pre>
 * </p>
 * <p>
 *     如果要清空此类的所有的解析器组合,可以调用方法{@link #clear()}
 * </p>
 * <p>
 *     所有需要进行方法参数解析的类，都可以直接借助此类实现参数的解析，比如mvc框架中的HandlerAdapter实现
 *     、ExceptionResolver以及ComplexTypeMethodArgumentResolver的实现，不同的类型可以借助此类组合自己所需要的一套参数解析器，
 *     比如HandlerAdapter可以利用此类组合几个解析器来解析参数，而异常解析器也可以自己组合另一套解析器供自己使用
 * </p>
 *
 * <p>使用此类是使用某个具体的解析器是一样的用法，都是先调用{@link #supports(MethodParameter)}方法，返回true后,
 * 再调用{@link #resolveArgument(MethodParameter, HttpServletRequest)}
 * </p>
 *
 * <p>
 *     由于mvc框架的参数解析器主要是基于类型的，所以不管是那个方法的参数，只要参数的类型一样，其解析器都会是同一个，
 *     为了提高性能，这里用了缓存，只要找到某一个类型的缓存就保存到缓存中，下次再来查找时就直接从缓存中提取，具体见{@link #getArgumentResolver(MethodParameter)}
 *     实现，所以这种缓存容器是渐进式增长的，这一点与{@link WebTypeConverters#getTypeConverter(Class)} }是不一样的，
 *     后者类加载时已经全部缓存起来了，容量不会变化了。也与{@link com.nf.mvc.mapping.RequestMappingHandlerMapping#getHandler(HttpServletRequest)}实现的缓存
 *     逻辑不太一样，后者容量超过设定的阈值后会清理掉超量的缓存项，此类永不进行缓存的清理操作
 * </p>
 *
 * <h3>使用注意事项</h3>
 * <p>想用此类解析参数时，直接调用{@link #resolveArgument(MethodParameter, HttpServletRequest)}方法即可，
 * 不需要先调用方法{@link #supports(MethodParameter)}</p>
 *
 * @see com.nf.mvc.adapter.RequestMappingHandlerAdapter
 * @see com.nf.mvc.support.MethodInvoker
 * @see BeanMethodArgumentResolver
 * @see com.nf.mvc.HandlerExceptionResolver
 */
public class MethodArgumentResolverComposite implements MethodArgumentResolver {

    private final List<MethodArgumentResolver> argumentResolvers = new ArrayList<>();
    private final Map<MethodParameter, MethodArgumentResolver> resolverCache = new ConcurrentHashMap<>(16);

    @Override
    public boolean supports(MethodParameter parameter) {
        return getArgumentResolver(parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request) throws Exception {
        MethodArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalArgumentException("不支持的参数类型 [" +
                    parameter.getParameterType() + "]. 当前解析的参数名是:[" + parameter.getParameterName()
                    + "],所在的方法是:[" + parameter.getMethod().getName() + "]，所在的类是:[" + parameter.getContainingClass().getName() + "]");
        }
        return resolver.resolveArgument(parameter, request);
    }

    private MethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        MethodArgumentResolver result = resolverCache.get(parameter);
        if (result == null) {
            for (MethodArgumentResolver argumentResolver : argumentResolvers) {
                if (argumentResolver.supports(parameter)) {
                    result = argumentResolver;
                    resolverCache.put(parameter, argumentResolver);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 此方法创建的实例已经包含mvc框架内提供的解析器与用户提供的定制解析器集合了，你可以在此基础上额外再添加一些解析器<br/>
     * <p><b><i>注意：此默认实例中的解析器集合并不包含此类型的实例</i></b></p>
     * @return HandlerMethodArgumentResolverComposite，里面包含着真正有解析能力的参数解析器集合
     */
    public static MethodArgumentResolverComposite defaultInstance() {
        return new MethodArgumentResolverComposite()
                .addResolvers(MvcContext.getMvcContext().getArgumentResolvers());

    }


    public MethodArgumentResolverComposite addResolver(MethodArgumentResolver resolver) {
        argumentResolvers.add(resolver);
        return this;
    }

    public MethodArgumentResolverComposite addResolvers(MethodArgumentResolver... resolvers) {
        if (resolvers != null) {
            Collections.addAll(this.argumentResolvers, resolvers);
        }
        return this;
    }

    public MethodArgumentResolverComposite addResolvers(List<MethodArgumentResolver> resolvers) {
        if (resolvers != null) {
            this.argumentResolvers.addAll(resolvers);
        }
        return this;
    }

    public MethodArgumentResolverComposite insertResolver(MethodArgumentResolver resolver) {
        argumentResolvers.add(0, resolver);
        return this;
    }

    public MethodArgumentResolverComposite insertResolvers(MethodArgumentResolver... resolvers) {
        if (resolvers != null) {
            List<MethodArgumentResolver> resolverList = Arrays.stream(resolvers).collect(Collectors.toList());
            argumentResolvers.addAll(0, resolverList);
        }
        return this;
    }

    public MethodArgumentResolverComposite insertResolvers(List<MethodArgumentResolver> resolvers) {
        if (resolvers != null) {
            argumentResolvers.addAll(0, resolvers);
        }
        return this;
    }

    public List<MethodArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(this.argumentResolvers);
    }

    public void clear() {
        this.argumentResolvers.clear();
    }
}
