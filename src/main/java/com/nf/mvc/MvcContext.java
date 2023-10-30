package com.nf.mvc;

import com.nf.mvc.configuration.ConfigurationProperties;
import com.nf.mvc.configuration.YmlParser;
import com.nf.mvc.support.OrderComparator;
import com.nf.mvc.util.ReflectionUtils;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.*;

/**
 * mvc框架的上下文类，通过此类主要是获取只读的框架类型信息，此类的内容是在{@link DispatcherServlet}
 * 初始化的时候就已经获取并实例化完毕，之后就不在变动了，也就是说这里的这些类型都是单例的
 * <p>
 *     MvcContext类是一个单例实现，想获取其实例，调用{@link #getMvcContext()} 即可
 * </p>
 *
 * <p>
 *     此类有如下一些类型信息可以获取
 *     <ol>
 *         <li>扫描到的所有Class信息，这些类已经加载，但没有实例化，通过{@link #getAllScannedClasses()}获取</li>
 *         <li>获取mvc框架的核心组件，核心组件指的是如下几个,这些组件分为mvc框架内部提供的与用户提供的定制(custom)组件,这些组件分别用对用的getXxx方法来获取
 *              <ul>
 *                  <li>{@link HandlerMapping}</li>
 *                  <li>{@link HandlerAdapter}</li>
 *                  <li>{@link MethodArgumentResolver}</li>
 *                  <li>{@link HandlerExceptionResolver}</li>
 *              </ul>
 *         </li>
 *         <li>获取配置器，通过{@link #getCustomWebMvcConfigurer()}</li>
 *         <li>获取拦截器，通过{@link #getCustomHandlerInterceptors()} }</li>
 *     </ol>
 * </p>
 * @see DispatcherServlet
 */
public class MvcContext {

    private static final MvcContext INSTANCE = new MvcContext();

    private ScanResult scanResult;

    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
    private List<MethodArgumentResolver> argumentResolvers = new ArrayList<>();
    private List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();

    private final List<Class<?>> allScannedClasses = new ArrayList<>();
    private final List<HandlerMapping> customHandlerMappings = new ArrayList<>();
    private final List<HandlerAdapter> customHandlerAdapters = new ArrayList<>();
    private final List<MethodArgumentResolver> customArgumentResolvers = new ArrayList<>();
    private final List<HandlerExceptionResolver> customExceptionResolvers = new ArrayList<>();
    private final List<HandlerInterceptor> customInterceptors = new ArrayList<>();
    private final List<MvcConfigurer> customConfigurers = new ArrayList<>();
    private final Map<Class<?>,Object> configurationProperties = new HashMap<>(16);

    private MvcContext() {
    }

    public static MvcContext getMvcContext() {
        return INSTANCE;
    }

    /**
     * 设置成public修饰符，框架使用者就可以直接修改这个扫描结果,这样不安全，
     * 所以这里改成默认修饰符，这样就只能在本包或子包中访问，基本上就是mvc框架内可以访问
     * <p>
     * 在整个mvc框架中，这些用户扩展的定制组件总是优先于mvc框架提供的同类型组件，定制组件之间可以通过{@link com.nf.mvc.support.Order}注解调整顺序
     * </p>
     * <p>
     * 目前扫描的是各种各样的类，没有规定只扫描Handler，比如有HandlerMapping，也有HandlerAdapter以及Handler等
     * <p>
     * @param scanResult ClassGraph的扫描结果
     */
    void resolveScannedResult(ScanResult scanResult) {
        this.scanResult = scanResult;
        ClassInfoList allClasses = scanResult.getAllClasses();
        for (ClassInfo classInfo : allClasses) {
            // 这些扫描到的类使用的类加载器与DispatcherServlet的类加载器是同一个
            Class<?> scannedClass = classInfo.loadClass();
            allScannedClasses.add(scannedClass);
            //配置属性类的解析必须优先于其它类型的解析
            resolveConfigurationProperties(scannedClass);
            resolveMvcClasses(scannedClass);
        }
    }

    /**
     * 解析扫描到的类是否是mvc框架核心功能类
     * <p>解析参数解析器要放在解析HandlerAdapter之前,因为一些HandlerAdapter的构造函数用到了参数解析器,
     * Mvc框架并不是一个容器管理框架,并没有对bean的依赖顺序进行管理</p>
     * @param scannedClass 所有扫描到的类
     */
    private void resolveMvcClasses(Class<?> scannedClass) {
        resolveMvcClass(scannedClass, MethodArgumentResolver.class, customArgumentResolvers);
        resolveMvcClass(scannedClass, HandlerMapping.class, customHandlerMappings);
        resolveMvcClass(scannedClass, HandlerAdapter.class, customHandlerAdapters);
        resolveMvcClass(scannedClass, HandlerExceptionResolver.class, customExceptionResolvers);
        resolveMvcClass(scannedClass, HandlerInterceptor.class, customInterceptors);
        resolveMvcClass(scannedClass, MvcConfigurer.class, customConfigurers);
    }

    private <T> void resolveMvcClass(Class<?> scannedClass, Class<? extends T> mvcInf, List<T> list) {
        if (mvcInf.isAssignableFrom(scannedClass)) {
            T instance = (T) ReflectionUtils.newInstance(scannedClass);
            list.add(instance);
        }
    }

    private void resolveConfigurationProperties(Class<?> scannedClass) {
        if (scannedClass.isAnnotationPresent(ConfigurationProperties.class)) {
            String prefix = scannedClass.getDeclaredAnnotation(ConfigurationProperties.class).value();
            Object instance = YmlParser.getInstance().parse(prefix, scannedClass);
            configurationProperties.put(scannedClass,instance);
        }
    }

    public List<HandlerMapping> getCustomHandlerMappings() {
        customHandlerMappings.sort(new OrderComparator<>());
        return Collections.unmodifiableList(customHandlerMappings);
    }

    public List<HandlerAdapter> getCustomHandlerAdapters() {
        customHandlerAdapters.sort(new OrderComparator<>());
        return Collections.unmodifiableList(customHandlerAdapters);
    }

    public List<MethodArgumentResolver> getCustomArgumentResolvers() {
        customArgumentResolvers.sort(new OrderComparator<>());
        return Collections.unmodifiableList(customArgumentResolvers);
    }

    public List<HandlerExceptionResolver> getCustomExceptionResolvers() {
        customExceptionResolvers.sort(new OrderComparator<>());
        return Collections.unmodifiableList(customExceptionResolvers);
    }

    public List<HandlerInterceptor> getCustomHandlerInterceptors() {
        customInterceptors.sort(new OrderComparator<>());
        return Collections.unmodifiableList(customInterceptors);
    }

    public MvcConfigurer getCustomWebMvcConfigurer() {
        if (customConfigurers.size() >1) {
            throw new IllegalStateException("配置器应该只写一个");
        }
        return customConfigurers.size()==0?null:customConfigurers.get(0);
    }

    public Map<Class<?>, Object> getConfigurationProperties() {
        return Collections.unmodifiableMap(configurationProperties);
    }

    /**
     * 因为我们解析之后，结果就是固定的，如果直接返回List
     * 用户是可以更改集合里面的内容的，所以需要返回一个只读集合
     *
     * @return 所有的HandlerMapping
     */
    public List<HandlerMapping> getHandlerMappings() {
        return Collections.unmodifiableList(handlerMappings);
    }

    public List<HandlerAdapter> getHandlerAdapters() {
        return Collections.unmodifiableList(handlerAdapters);
    }

    public List<MethodArgumentResolver> getArgumentResolvers() {
        return Collections.unmodifiableList(argumentResolvers);
    }

    public List<HandlerExceptionResolver> getExceptionResolvers() {
        return Collections.unmodifiableList(exceptionResolvers);
    }

    public List<Class<?>> getAllScannedClasses() {
        return Collections.unmodifiableList(allScannedClasses);
    }

    /**
     * 以下这些方法是默认修饰符，主要是在框架内调用，用户不能调用
     * @return ClassGraph的扫描结果
     */
    ScanResult getScanResult() {
        return this.scanResult;
    }

    void setHandlerMappings(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    void setHandlerAdapters(List<HandlerAdapter> handlerAdapters) {
        this.handlerAdapters = handlerAdapters;
    }

    void setArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        this.exceptionResolvers = exceptionResolvers;
    }

}
