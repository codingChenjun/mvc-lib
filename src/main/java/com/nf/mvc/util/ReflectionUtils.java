package com.nf.mvc.util;

import com.nf.mvc.MvcContext;
import com.nf.mvc.argument.BeanMethodArgumentResolver;
import com.nf.mvc.argument.MethodParameter;
import com.nf.mvc.handler.HandlerClass;
import com.nf.mvc.ioc.Injected;
import javassist.Modifier;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.*;
import java.util.*;

/**
 * 此类的代码参考了spring的ReflectionUtils,BeanUtils
 * 此类的功能暂定只处理对类的实例化,方法,字段,方法参数进行处理,其它功能可以放到其它类型中去处理
 */
public abstract class ReflectionUtils {

    private static final String GETTER_METHOD_Pattern = "^get[A-Z].*";
    private static final String GETTER_IS_METHOD_Pattern = "^is[A-Z].*";
    private static final String SETTER_METHOD_Pattern = "^set[A-Z].*";

    /**
     * 现在这种写法是调用class的默认构造函数来实例化对象的,暂时没有考虑调用其它构造函数进行实例化的情况
     * <h3>使用地方</h3>
     * <p>整个mvc框架都用的这个方法来创建被mvc管理的类的对象，使用的地方有以下几个
     * <ul>
     *     <li>实例化扫描到的Mvc核心类，详见{@link com.nf.mvc.MvcContext#resolveMvcClass(Class, Class, List)},这些类型是单例的</li>
     *     <li>实例化控制器bean类型的方法参数，详见{@link BeanMethodArgumentResolver#resolveSetterArgument(MethodParameter, HttpServletRequest, Stack)},这些实例是原型的</li>
     *     <li>实例化用户编写的后端控制器，详见{@link HandlerClass#getHandlerObject()},这些实例是原型的</li>
     * </ul>
     * </p>
     *
     * @param clz 用来实例化的class
     * @return 此class的实例
     */
    public static <T> T newInstance(Class<? extends T> clz) {
        T instance;
        try {
            instance = clz.newInstance();
            injectConfigurationProperties(instance);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("无法实例化对象，类:" + clz.getName() + " 是否没有提供默认构造函数?", e);
        }
        return instance;
    }

    /**
     * 此方法目前只是用来注入配置属性类使用的
     * <p>此方法本不应该写在这里,因为它与注入有关,但不想增加复杂性,
     * 也不想给mvc框架提供ioc的能力,就简单注入配置属性类,所以就写在了这里</p>
     *
     * <p>此方法也应该设计为抛出Exception更好，这里抛出的调用setAccessible方法时产生的异常，
     * 主要是为了在{@link #newInstance(Class)}方法里演示catch的或（|）写法</p>
     *
     * @param instance 某个需要注入配置属性的实例
     * @param <T>      实例的类型
     * @throws IllegalAccessException 注入ConfigurationProperties时产生的异常
     */
    private static <T> void injectConfigurationProperties(T instance) throws IllegalAccessException {
        Field[] fields = instance.getClass()
                .getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Injected.class)) {
                field.setAccessible(true);
                Object configProperties = MvcContext.getMvcContext()
                        .getConfigurationProperties()
                        .get(field.getType());
                field.set(instance, configProperties);
                field.setAccessible(false);
            }
        }
    }

    public static List<Method> getAllSetterMethods(Class<?> clz) {
        List<Method> setterMethods = new ArrayList<>();
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (ReflectionUtils.isSetter(method)) {
                setterMethods.add(method);
            }
        }
        return setterMethods;
    }

    /**
     * <a href="https://www.runoob.com/regexp/regexp-syntax.html">正则表达式入门教程</a>
     * <ul>
     * <li>^:表示以什么开始，^set意思就是以set开头</li>
     * <li>[A-Z] ： 表示一个区间，匹配所有大写字母，[a-z] 表示所有小写字母</li>
     * <li>句号：匹配除换行符（\n、\r）之外的任何单个字符</li>
     * <li>‘*’：匹配前面的子表达式零次或多次,在下面的例子就是前面的句号</li>
     * </ul>
     * <p>所以^set[A-Z].* 意思就是以set开头，之后跟一个大写字母，大写字母之后可以出现0个或多个字符</p>
     *
     * @param method 方法
     * @return 是setter方法就返回true，否则返回false
     */
    public static boolean isSetter(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType()
                        .equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName()
                        .matches(SETTER_METHOD_Pattern);
    }

    public static boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
                method.getParameterTypes().length == 0) {
            if (method.getName()
                    .matches(GETTER_METHOD_Pattern) &&
                    !method.getReturnType()
                            .equals(void.class)) {
                return true;
            }
            return method.getName()
                    .matches(GETTER_IS_METHOD_Pattern) &&
                    method.getReturnType()
                            .equals(boolean.class);
        }
        return false;
    }

    public static void setFieldValue(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("字段值设置失败", e);
        }
    }

    public static List<String> getParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        List<String> parameterNames = new ArrayList<>();

        for (Parameter parameter : parameters) {
            if (!parameter.isNamePresent()) {
                throw new IllegalStateException("编译的时候需要指定-parameters选项");
            }
            String parameterName = parameter.getName();
            parameterNames.add(parameterName);
        }
        return parameterNames;
    }

    /**
     * 此方法是用来获取方法泛型参数的类型实参的
     *
     * @param parameter 方法参数信息,要求是一个泛型实参类型
     * @return 返回所有的泛型实参类型信息
     */
    public static Class<?>[] getActualArgument(Parameter parameter) {

        Type type = parameter.getParameterizedType();
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("参数要求是一个参数化的泛型类型，不能使用原生类型");
        }

        // 如果方法的参数是List这样的类型，而不是List<String>,List<Integer>这样的，直接进行类型转换抛出ClassCastException异常
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] types = parameterizedType.getActualTypeArguments();
        Class<?>[] actualTypeArguments = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            actualTypeArguments[i] = (Class<?>) types[i];
        }
        return actualTypeArguments;
    }
    // region 废弃的
    //---------------------------------------------------------------------
    // 下面的是存放废弃的方法,不删除是为了让学生也了解这些信息,
    // 下面2个利用javassist库获取方法参数的功能不完善,有bug
    //---------------------------------------------------------------------

    /**
     * 参考
     * <a href="https://blog.csdn.net/hehuanchun0311/article/details/79755266">...</a>
     * 与 https://blog.csdn.net/wwzmvp/article/details/116302782
     * <a href="http://lzxz1234.github.io/java/2014/07/25/Get-Method-Parameter-Names-With-Javassist.html">...</a>
     *
     * @param clazz:方法所在的类
     * @param methodName：方法的名字
     * @param paramTypes：方法的参数类型，以便支持重载
     * @return 方法各个参数的名字（依据参数位置顺序依次返回）
     */
    @Deprecated
    public static List<String> getParamNamesWithParamType(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        List<String> paramNames = new ArrayList<>();
        ClassPool pool = ClassPool.getDefault();
        int paramLength = 0;
        /*不加下面的insertClassPath这行代码会出现ClassNotFound异常，解决办法如下
        含义是告诉javassist也去ReflectionUtils类所在的类路径下去查找类*/
        pool.insertClassPath(new ClassClassPath(ReflectionUtils.class));
        try {
            CtClass ctClass = pool.getCtClass(clazz.getName());
            CtMethod ctMethod;
            if (paramTypes != null && paramTypes.length > 0) {
                CtClass[] paramClasses = new CtClass[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    paramClasses[i] = pool.get(paramTypes[i].getName());
                }
                ctMethod = ctClass.getDeclaredMethod(methodName, paramClasses);
            } else {
                ctMethod = ctClass.getDeclaredMethod(methodName);
                paramLength = ctMethod.getParameterTypes().length;
            }
            // 使用javassist的反射方法的参数名
            javassist.bytecode.MethodInfo methodInfo = ctMethod.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            if (attr != null) {
                // 必须是对索引进行排序，所以选定一个可以排序的集合
                TreeMap<Integer, String> sortMap = new TreeMap<>();
                for (int i = 0; i < attr.tableLength(); i++) {
                    sortMap.put(attr.index(i), attr.variableName(i));
                }
                int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
                paramNames = Arrays.asList(Arrays.copyOfRange(sortMap.values()
                        .toArray(new String[0]), pos, paramLength + pos));
                return paramNames;
            }
            return paramNames;
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Deprecated
    public static List<String> getParamNames(Class<?> clazz, String methodName) {
        return getParamNamesWithParamType(clazz, methodName);
    }
    // endregion
}
