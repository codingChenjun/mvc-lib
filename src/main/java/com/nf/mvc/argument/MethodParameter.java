package com.nf.mvc.argument;

import com.nf.mvc.handler.HandlerMethod;
import com.nf.mvc.util.ClassUtils;
import com.nf.mvc.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

/**
 * 此类是某个方法的某一个参数的封装类，里面主要封装了
 * <ul>
 *     <li>{@link Parameter}</li>
 *     <li>{@link Method}</li>
 *     <li>参数名:{@link #getParameter()}</li>
 *     <li>参数在方法中的位置，以0开头:{@link #getParameterIndex()} ()}</li>
 *     <li>参数所在方法所在的类:{@link #getContainingClass()} ()} ()}</li>
 * </ul>
 * <h3>关于参数名</h3>
 * <p>优先以RequestParam注解指定的值为准，如果没有指定，就以反射获取的名字为准，
 * 方法{@link #getParameter()}实现了这个逻辑。所以，所有的方法参数解析器都支持注解指定的参数名方式，
 * 而RequestParam注解指定的默认值，并没有写在这个类里面，是因为默认值并不是对所有的参数解析器有用，默认值主要是针对简单类型有用</p>
 * <h3>hashCode与equals的重写</h3>
 * <p>
 *     这个类之所以重写这些内容是因为参数类型对应的解析器我进行了缓存的处理，而缓存又是以此类型作为key值的。
 * </p>
 * <h3>isList与isArray</h3>
 * <p>
 *     关于这两个方法及其相关的方法放在这里的原因是，本mvc框架对参数进行解析的时候，主要只支持T,T[],List<T>这三种情况，
 *     放在这里便于解析器调用，也不存在非常不合理的逻辑
 * </p>
 * <h3>为什么不持有HandlerMethod，而是持有Method</h3>
 * <p>
 *     HandlerMethod包含Method也有一些便利的API，比如{@link HandlerMethod#getParameterCount()}之类的,
 *     主要的原因是Method的通用性更强，HandlerMethod毕竟是mvc框架内的一个类型，所以并不适合在出现Method的地方，全部用HandlerMethod取代。
 *     比如ReflectionUtils的getParameterNames方法与MethodInvoker的invoke这些比较通用型的类型，是不应该使用HandlerMethod这个独属于Mvc框架的类的。
 *     鉴于这些通用型的类型也用到了MethodParameter，所以为了协调，这里也没有用HandlerMethod。
 * </p>
 *
 * @see com.nf.mvc.MethodArgumentResolver
 * @see com.nf.mvc.support.MethodInvoker
 * @see HandlerMethod
 * @see RequestParam
 */
public class MethodParameter {
    private final Method method;
    /**
     * 本类不要直接使用此变量，要通过getParameter方法来使用parameter变量，
     * 因为构造函数并没有初始化这个变量，直接使用可能报空引用异常
     */
    private Parameter parameter;
    private String parameterName;
    private final int parameterIndex;
    private final Class<?> containingClass;

    public MethodParameter(Method method, int parameterIndex, String parameterName) {
        this(method, parameterIndex, parameterName, method.getDeclaringClass());
    }

    public MethodParameter(Method method, int parameterIndex, String parameterName, Class<?> containingClass) {
        this.method = method;
        this.parameterIndex = parameterIndex;
        this.parameterName = parameterName;
        this.containingClass = containingClass;
    }

    public Class<?> getParameterType() {
        return getParameter().getType();
    }

    public Parameter getParameter() {
        if (this.parameterIndex < 0) {
            throw new IllegalStateException("无效的参数索引");
        }
        Parameter parameter = this.parameter;
        if (parameter == null) {
            parameter = method.getParameters()[this.parameterIndex];
            this.parameter = parameter;
        }
        return parameter;
    }

    /**
     * 获取参数的名字，如果方法参数有注解RequestParam修饰，并指定了名字就以注解指定的为准，
     * 否则，靠反射解析方法参数名，这就需要在编译的时候添加-parameters选项
     *
     * @return 返回参数名
     */
    public String getParameterName() {
        // 参数上有注解，可能只是用来设置默认值，没有设置value
        if (getParameter().isAnnotationPresent(RequestParam.class)) {
            String value = getParameter().getDeclaredAnnotation(RequestParam.class)
                    .value();
            // 没有设置value，value就会保留默认值，这个值我们不采用，仍然用方法的参数名（通过反射解析出来的）
            // 如果你有注解，并且设置了value，我们才采用value属性的值作为参数名
            if (!value.equals(ValueConstants.DEFAULT_NONE)) {
                this.parameterName = value;
            }
        }
        return this.parameterName;
    }

    public Method getMethod() {
        return method;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public Class<?> getContainingClass() {
        return containingClass;
    }

    public boolean isArray() {
        return getParameterType().isArray();
    }

    public boolean isList() {
        return ClassUtils.isList(getParameterType());
    }

    public boolean isSimpleType() {
        return ClassUtils.isSimpleType(getParameterType());
    }

    /**
     * 判断方法的参数是不是一个参数化的类型，比如List<String>这种写法就是一个参数化类型，
     * 也就是一个泛型实参化的类型
     *
     * @return true表示是一个泛型实参化的类型，否则是false
     */
    public boolean isParameterizedType() {
        return getParameter().getParameterizedType() instanceof ParameterizedType;
    }

    public Class<?> getComponentType() {
        if (isArray()) {
            return getParameterType().getComponentType();
        }
        throw new IllegalStateException("不是数组，无法获取组件类型");
    }

    /**
     * 这个方法主要是为了获取List<String>这样的方法参数的实参String这样的类型
     *
     * @return 返回第一个泛型实参类型
     */
    public Class<?> getFirstActualTypeArgument() {
        return getActualArguments()[0];
    }

    /**
     * 此方法是用来获取方法泛型参数的类型实参的。
     *
     * @return 返回所有的泛型实参类型数组
     */
    public Class<?>[] getActualArguments() {
        return ReflectionUtils.getActualArgument(getParameter());
    }

    public boolean isPresent(Class<? extends Annotation> annotationClass) {
        return getParameter().isAnnotationPresent(annotationClass);
    }

    public Object getDefaultValue() {
        if (getParameter().isAnnotationPresent(RequestParam.class)) {
            String defaultValue = getParameter().getDeclaredAnnotation(RequestParam.class)
                    .defaultValue();
            // 用户可能只是利用RequestParam设置了参数名字，没有设置默认值
            if (!defaultValue.equals(ValueConstants.DEFAULT_NONE)) {
                return defaultValue;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        // 1.自己与其它是同一个（==）对象，那么肯定相等
        if (this == other) {
            return true;
        }
        // 2.别人跟我根本不是同一个类型。所以就一定不相等
        if (!(other instanceof MethodParameter)) {
            return false;
        }
        // 3.代码到这里，就意味着绝不是同一个对象，但类型是一样的
        // 参数位置一样，所在方法一样，所在类一样
        MethodParameter otherParam = (MethodParameter) other;
        return (getContainingClass() == otherParam.getContainingClass() &&
                this.parameterIndex == otherParam.parameterIndex &&
                this.getMethod()
                        .equals(otherParam.getMethod()));
    }

    @Override
    public int hashCode() {
        return (31 * this.getMethod()
                .hashCode() + this.parameterIndex);
    }
}
