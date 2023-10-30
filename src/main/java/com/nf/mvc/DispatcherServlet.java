package com.nf.mvc;

import com.nf.mvc.adapter.HttpRequestHandlerAdapter;
import com.nf.mvc.adapter.RequestMappingHandlerAdapter;
import com.nf.mvc.argument.BeanMethodArgumentResolver;
import com.nf.mvc.argument.MethodArgumentResolverComposite;
import com.nf.mvc.argument.MethodParameter;
import com.nf.mvc.argument.MultipartFileMethodArgumentResolver;
import com.nf.mvc.argument.PathVariableMethodArgumentResolver;
import com.nf.mvc.argument.RequestBodyMethodArgumentResolver;
import com.nf.mvc.argument.ServletApiMethodArgumentResolver;
import com.nf.mvc.argument.SimpleTypeMethodArgumentResolver;
import com.nf.mvc.cors.CorsConfiguration;
import com.nf.mvc.exception.ExceptionHandlerExceptionResolver;
import com.nf.mvc.exception.LogHandlerExceptionResolver;
import com.nf.mvc.exception.ParameterizedExceptionHandlersExceptionResolver;
import com.nf.mvc.exception.PrintStackTraceHandlerExceptionResolver;
import com.nf.mvc.mapping.NameConventionHandlerMapping;
import com.nf.mvc.mapping.RequestMappingHandlerMapping;
import com.nf.mvc.support.Delimiters;
import com.nf.mvc.support.HttpHeaders;
import com.nf.mvc.support.HttpMethod;
import com.nf.mvc.util.CorsUtils;
import com.nf.mvc.util.ScanUtils;
import com.nf.mvc.util.StringUtils;
import io.github.classgraph.ScanResult;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 此类是一个前端控制器，我也喜欢称其为总控器，此Servlet类是请求进入到我们的mvc框架的入口部分。<br/><br/>
 *
 * <h3>基本使用</h3>
 * <p>
 * 用户通常应该只配置一个这样的Servlet，并且为其url-pattern设置值为"/",虽然理论上你可以配置多个这样的servlet，但很少见。
 * 而为了让其支持文件上传，通常也要配置multipart-config，如果想让servlet容器启动时就执行DispatcherServlet的初始化逻辑，
 * 通常也会配置load-on-startup选项,典型的配置如下:
 * <pre class="xml">
 *
 *   < servlet>
 *      < servlet-name>mvcDemo< /servlet-name>
 *      < servlet-class>com.nf.mvc.DispatcherServlet< /servlet-class>
 *      < init-param>
 *          < param-name>base-package< /param-name>
 *          <param-value>mvcDemo.web< /param-value>
 *      </init-param>
 *      < load-on-startup>200< /load-on-startup>
 *      < multipart-config>
 *      < /multipart-config>
 *  < /servlet>
 *
 *  < servlet-mapping>
 *      < servlet-name>mvcDemo</servlet-name>
 *      < url-pattern>/</url-pattern>
 *  < /servlet-mapping>
 * }
 * </pre>
 * </p>
 * <h3>默认组件与自定义组件</h3>
 * <p>
 * 整个mvc框架扩展用的组件分为框架提供的默认组件与用户提供的自定义组件，同类型的自定义组件优先级总是高于框架提供的的默认组件的，
 * 同类型的自定义组件的优先级可以通过Order注解调整。<br/>
 * 整个Mvc框架管理有7大组件，其中前4大组件是只要编写对应的实现类，放置在项目中即可，Mvc框架通过类扫描的方式获取并应用到框架上，不需要再在别的地方使用或配置，
 * 而第五大组件ViewResult，用户通常采用继承此类来扩展Mvc框架的能力，并应用在控制器方法的返回值上，第6类组件是用来对Mvc框架内置的5大组件(HandlerMapping,HandlerAdapter,
 * MethodArgumentResolver,HandlerExceptionResolver,CorsConfiguration)进行配置用的，最后一个拦截器组件，是用户用来编写项目的拦截相关的业务使用，
 * 比如实现验证方面的拦截器
 * <ul>
 *     <li>{@link HandlerMapping}</li>
 *     <li>{@link HandlerAdapter}</li>
 *     <li>{@link MethodArgumentResolver}</li>
 *     <li>{@link HandlerExceptionResolver}</li>
 *     <li>{@link ViewResult}</li>
 *     <li>{@link MvcConfigurer}</li>
 *     <li>{@link HandlerInterceptor}</li>
 * </ul>
 * </p>
 * <h3>组件的获取</h3>
 * <p>
 * 用户自定义组件是通过类扫描的方式获取的，用户在配置DispatcherServlet的时候通过参数<i>{@code base-package }</i>指定扫描的基础包，
 * 框架会扫描指定包及其子包下的所有类型，并加载到jvm，所以，强烈建议指定的包，只包含web层面的一些组件，不要指定dao，service相关的类所在的包，
 * web层面的类型主要有如下一些类型
 *    <ul>
 *       <li>用户创建的后端控制器</li>
 *       <li>用户创建的拦截器</li>
 *       <li>用户创建的WebMvcConfigurer</li>
 *    </ul>
 * </p>
 * <h3>核心组件的实例化</h3>
 * <p>
 *   核心的mvc框架组件都是在此类的{@link #init(ServletConfig)}方法实例化并完成组合的。可以看任何一个以init开头的方法了解详情，
 *   比如{@link #initHandlerMappings()},7大被Mvc框架处理的组件都要求必须有默认构造函数
 * </p>
 * <h3>初始化处理</h3>
 * <ul>
 *     <li>扫描指定包及其子包下的所有类型</li>
 *     <li>创建MvcContext实例</li>
 *     <li>初始化Mvc框架
 *          <ol>
 *              <li>初始化所有的参数解析器</li>
 *              <li>初始化所有的HandlerMapping</li>
 *              <li>初始化所有的HandlerAdapters</li>
 *              <li>初始化所有的异常解析器</li>
 *          </ol>
 *     </li>
 *     <li>配置Mvc框架：利用{@link MvcConfigurer}的实现类对Mvc框架内部组件进行配置</li>
 * </ul>
 * <h3>核心请求处理流程</h3>
 * <ol>
 *     <li>用户发起请求</li>
 *     <li>遍历所有的HandlerMapping，直到找到一个Handler处理请求，找不到就交给默认Servlet处理请求</li>
 *     <li>遍历所有的HandlerAdapter，直到找到一个支持此Handler的HandlerAdapter，找不到就抛异常</li>
 *     <li>HandlerAdapter开始负责Handler方法的调用执行
 *          <ol>
 *              <li>获取Handler类型的实例化</li>
 *              <li>遍历方法的每一个参数，解析出此参数的值，解析的时候是遍历每一个参数解析器，找到能支持的解析器就结束遍历，并利用解析器解析出值，
 *              如果找不到能解析的解析就抛出异常，具体见{@link MethodArgumentResolverComposite#resolveArgument(MethodParameter, HttpServletRequest)} </li>
 *              <li>执行Handler的方法</li>
 *              <li>适配Handler执行结果为ViewResult类型</li>
 *          </ol>
 *     </li>
 *     <li>对Handler的执行结果ViewResult进行渲染（render）</li>
 *     <li>如果Handler执行链出了异常交给异常解析器去处理</li>
 * </ol>
 * <h3>静态资源处理</h3>
 * <p>
 *     静态资源的地址如果没有对应HandlerMapping能处理，就进入到了默认Servlet的处理逻辑，
 *     而默认Servlet是可以处理静态资源的
 * </p>
 *
 *  <h3>cors</h3>
 *  <p>
 *  mvc框架只实现了全局跨域的处理，并没有对某个地址进行单独的跨域处理，所以，跨域的配置是影响到所有的url请求的，
 *  如果你不通过实现WebMvcConfigurer接口的方式配置跨域，那么它会采用默认值，具体的跨域配置情况见{@link CorsConfiguration#applyDefaultConfiguration()}
 *  方法里面的设置
 *  </p>
 *
 * @see MvcContext
 * @see MethodArgumentResolver
 * @see HandlerMapping
 * @see HandlerAdapter
 * @see HandlerExecutionChain
 * @see HandlerExceptionResolver
 * @see MvcConfigurer
 * @see CorsConfiguration
 */
public class DispatcherServlet extends HttpServlet {
  /**
   * 此选项是用来配置要扫描的类所在的基础包的，在DispatcherServlet的init-param里面进行配置
   */
  private static final String BASE_PACKAGE = "base-package";
  private final List<HandlerMapping> handlerMappings = new ArrayList<>();
  private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();
  private final List<MethodArgumentResolver> argumentResolvers = new ArrayList<>();
  private final List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();

  /**
   * 用这种方式实例化是因为其调用了applyDefaultConfiguration方法，创建出来的对象是有了一些默认设置的
   * 因为我现在的策略是：不管有没有配置器对跨域进行定制配置，都要全局进行跨域支持的处理
   */
  private final CorsConfiguration corsConfiguration = CorsConfiguration.defaultInstance();

  // region 初始化逻辑

  /**
   * 此方法完成mvc框架中的初始化逻辑，主要完成了如下工作
   * <ul>
   *     <li>扫描指定包下所有的类,但主要关注的是如下类型：
   *          <ul>
   *              <li>HandlerMapping</li>
   *              <li>MethodArgumentResolver</li>
   *              <li>HandlerAdapter</li>
   *              <li>HandlerExceptionResolver</li>
   *              <li>WebMvcConfigurer</li>
   *              <li>HandlerInterceptor</li>
   *              <li>Handler(可能是任何类型），mvc框架现在只支持方法有@RequestMapping修饰的类型</li>
   *          </ul>
   *     </li>
   *     <li>初始化MvcContext</li>
   *     <li>初始化Mvc框架核心组件</li>
   *     <li>配置Mvc框架提供的默认组件</li>
   * </ul>
   * <p>
   *     注意：初始化完毕之后，扫描的7大类型中，除了Handler其它6大类型全部已经被实例化了，
   *  并且也是在初始化阶段完成了4大核心组件的组装
   * </p>
   * <h3>关于单例与原型</h3>
   * <p>
   * 单例对象：只有一个实例，原型对象：每次都会实例化一个对象出来。<br/>
   * 所有在DispatcherServlet类的初始化时创建出来的对象基本都是单例的，也就是只会实例化一个对象，主要的单例对象如下
   *     <ol>
   *        <li>DispatcherServlet</li>
   *        <li>所有的HandlerMapping，包含默认与定制</li>
   *        <li>所有的HandlerAdapter，包含默认与定制</li>
   *        <li>所有的MethodArgumentResolver，包含默认与定制</li>
   *        <li>所有的HandlerExceptionResolver，包含默认与定制</li>
   *        <li>所有的定制拦截器HandlerInterceptor，mvc框架没有提供默认的拦截器实现</li>
   *     </ol>
   *     常见的原型对象有：
   *     <ol>
   *         <li>用户控制器类(用户编写)</li>
   *         <li>控制器方法的复杂类型的参数(用户编写)</li>
   *         <li>ViewResult对象(用户编写)</li>
   *         <li>MethodParameter</li>
   *         <li>HandlerMethod</li>
   *     </ol>
   * </p>
   *
   * @param config ServletConfig对象
   * @throws ServletException ServletException对象
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    // 获取要扫描的类所在的基础包的名字
    String[] basePackages = getBasePackages(config);
    // 去执行类扫描的功能
    ScanResult scanResult = ScanUtils.scan(basePackages);
        /*
            initMvc与configMvc方法设置为private final是为了阻止子类重写，因为mvc组件的处理是有先后顺序要求的，
            比如Adapter组件初始化时，是需要参数解析器初始化完毕的，子类要变更初始化逻辑，只需要重写单独的某个init方法即可
         */
    initMvcContext(scanResult);
    initMvc();
    configMvc();
  }

  private void initMvcContext(ScanResult scanResult) {
    MvcContext.getMvcContext()
            .resolveScannedResult(scanResult);
  }

  private void initMvc() {
    /* 参数解析器因为被Adapter使用，所以其初始化要在adapter初始化之前进行 */
    initArgumentResolvers();
    initHandlerMappings();
    initHandlerAdapters();
    initExceptionResolvers();
  }

  private void configMvc() {
    MvcContext mvcContext = MvcContext.getMvcContext();
    MvcConfigurer mvcConfigurer = mvcContext.getCustomWebMvcConfigurer();
    // 没有配置器，不需要配置，提前结束configMvc方法的执行
    if (mvcConfigurer == null) {
      return;
    }

    // 由于这些配置方法子类可以重写，所以给这些方法都设置了参数，便于重写，不考虑重写的话，这些方法是可以不用添加任何参数的
    configArgumentResolvers(MvcContext.getMvcContext()
            .getArgumentResolvers(), mvcConfigurer);
    configHandlerMappings(MvcContext.getMvcContext()
            .getHandlerMappings(), mvcConfigurer);
    configHandlerAdapters(MvcContext.getMvcContext()
            .getHandlerAdapters(), mvcConfigurer);
    configExceptionResolvers(MvcContext.getMvcContext()
            .getExceptionResolvers(), mvcConfigurer);
    // 由于corsConfiguration对象是有了默认值设置的实例，没有配置器的时候不配置cors也能用默认设置处理跨域
    configGlobalCors(this.corsConfiguration, mvcConfigurer);
  }

  protected void configArgumentResolvers(List<MethodArgumentResolver> argumentResolvers, MvcConfigurer mvcConfigurer) {
    executeMvcComponentsConfig(argumentResolvers, mvcConfigurer::configureArgumentResolver);
  }

  protected void configHandlerMappings(List<HandlerMapping> handlerMappings, MvcConfigurer mvcConfigurer) {
    executeMvcComponentsConfig(handlerMappings, mvcConfigurer::configureHandlerMapping);
  }

  protected void configHandlerAdapters(List<HandlerAdapter> handlerAdapters, MvcConfigurer mvcConfigurer) {
    executeMvcComponentsConfig(handlerAdapters, mvcConfigurer::configureHandlerAdapter);
  }

  protected void configExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers, MvcConfigurer mvcConfigurer) {
    executeMvcComponentsConfig(exceptionResolvers, mvcConfigurer::configureExceptionResolver);
  }

  protected void configGlobalCors(CorsConfiguration configuration, MvcConfigurer mvcConfigurer) {
    // 不需要再调用默认设置，全局实例化时已经设置过了，如果用户不需要这些默认设置，可以调用clearDefaultConfiguration方法进行清除
    // configuration.applyDefaultConfiguration();
    mvcConfigurer.configureCors(configuration);
    // mvcConfigurer.configureCors(configuration) 这行代码你也可以换成像下面这样写
    // executeMvcComponentsConfig(Arrays.asList(configuration),mvcConfigurer::configureCors);
  }

  private <T> void executeMvcComponentsConfig(List<T> mvcComponents, Consumer<T> consumer) {
    mvcComponents.forEach(consumer);
  }

  private void initArgumentResolvers() {

    List<MethodArgumentResolver> customArgumentResolvers = getCustomArgumentResolvers();

    List<MethodArgumentResolver> defaultArgumentResolvers = getDefaultArgumentResolvers();

    argumentResolvers.addAll(customArgumentResolvers);
    argumentResolvers.addAll(defaultArgumentResolvers);
    // 把定制+默认的所有HandlerMapping组件添加到上下文中
    MvcContext.getMvcContext()
            .setArgumentResolvers(argumentResolvers);
  }

  protected List<MethodArgumentResolver> getDefaultArgumentResolvers() {
    List<MethodArgumentResolver> argumentResolvers = new ArrayList<>();
    argumentResolvers.add(new ServletApiMethodArgumentResolver());
    argumentResolvers.add(new MultipartFileMethodArgumentResolver());
    // RequestBody解析器要放在复杂类型解析器之前，基本上简单与复杂类型解析器应该放在最后
    argumentResolvers.add(new RequestBodyMethodArgumentResolver());
    argumentResolvers.add(new PathVariableMethodArgumentResolver());
    argumentResolvers.add(new SimpleTypeMethodArgumentResolver());
    argumentResolvers.add(new BeanMethodArgumentResolver());

    return argumentResolvers;
  }

  protected List<MethodArgumentResolver> getCustomArgumentResolvers() {
    return MvcContext.getMvcContext()
            .getCustomArgumentResolvers();
  }

  private void initHandlerMappings() {
    // 优先添加用户自定义的HandlerMapping
    List<HandlerMapping> customHandlerMappings = getCustomHandlerMappings();
    // mvc框架自身的HandlerMapping优先级更低，后注册
    List<HandlerMapping> defaultHandlerMappings = getDefaultHandlerMappings();

    handlerMappings.addAll(customHandlerMappings);
    handlerMappings.addAll(defaultHandlerMappings);
    // 把定制+默认的所有HandlerMapping组件添加到上下文中
    MvcContext.getMvcContext()
            .setHandlerMappings(handlerMappings);
  }

  protected List<HandlerMapping> getCustomHandlerMappings() {
    return MvcContext.getMvcContext()
            .getCustomHandlerMappings();
  }

  protected List<HandlerMapping> getDefaultHandlerMappings() {
    List<HandlerMapping> mappings = new ArrayList<>();
    mappings.add(new RequestMappingHandlerMapping());
    mappings.add(new NameConventionHandlerMapping());
    return mappings;
  }

  private void initHandlerAdapters() {
    List<HandlerAdapter> customHandlerAdapters = getCustomHandlerAdapters();
    List<HandlerAdapter> defaultHandlerAdapters = getDefaultHandlerAdapters();

    handlerAdapters.addAll(customHandlerAdapters);
    handlerAdapters.addAll(defaultHandlerAdapters);
    MvcContext.getMvcContext()
            .setHandlerAdapters(handlerAdapters);

  }

  protected List<HandlerAdapter> getCustomHandlerAdapters() {
    return MvcContext.getMvcContext()
            .getCustomHandlerAdapters();
  }

  protected List<HandlerAdapter> getDefaultHandlerAdapters() {
    List<HandlerAdapter> adapters = new ArrayList<>();
    adapters.add(new RequestMappingHandlerAdapter());
    adapters.add(new HttpRequestHandlerAdapter());
    // handlerAdapters.add(new MethodNameHandlerAdapter());
    return adapters;
  }

  private void initExceptionResolvers() {
    List<HandlerExceptionResolver> customExceptionResolvers = getCustomExceptionResolvers();
    List<HandlerExceptionResolver> defaultExceptionResolvers = getDefaultExceptionResolvers();
    exceptionResolvers.addAll(customExceptionResolvers);
    exceptionResolvers.addAll(defaultExceptionResolvers);
    MvcContext.getMvcContext()
            .setExceptionResolvers(exceptionResolvers);
  }

  protected List<HandlerExceptionResolver> getCustomExceptionResolvers() {
    return MvcContext.getMvcContext()
            .getCustomExceptionResolvers();
  }

  protected List<HandlerExceptionResolver> getDefaultExceptionResolvers() {
    List<HandlerExceptionResolver> resolvers = new ArrayList<>();
    resolvers.add(new LogHandlerExceptionResolver());
    resolvers.add(new PrintStackTraceHandlerExceptionResolver());
    resolvers.add(new ExceptionHandlerExceptionResolver());
    resolvers.add(new ParameterizedExceptionHandlersExceptionResolver());
    return resolvers;
  }

  private String[] getBasePackages(ServletConfig config) {
    String pkg = config.getInitParameter(BASE_PACKAGE);
    if (pkg == null || pkg.isEmpty()) {
      throw new IllegalStateException("必须指定扫描的包，此包是控制器或者是其它Mvc框架扩展组件所在的包");
    }
    return StringUtils.split(pkg, Delimiters.Common.getPattern())
            .toArray(new String[]{});
  }

  // endregion
  // region 请求处理逻辑

  /**
   * 方法通常会包含很多核心逻辑步骤，如果每一个逻辑步骤都有一些零散的代码
   * 如果都放在一起，就会导致本方法代码很长，不容易看懂，
   * 所以，最好是把其中一个个的核心逻辑用单独的方法封装起来
   * <p>
   * service的方法，由于是重写父类型的方法，其签名是没有办法改变
   * 比如改成throws Throwable，这是不行的
   * <p>
   * 所以就增加了一个doService的方法，以便有机会改doService的签名
   *
   * @param req  请求对象
   * @param resp 响应对象
   * @throws ServletException Servlet异常
   * @throws IOException      IO异常
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    setEncoding(req, resp);
    if (CorsUtils.isCorsRequest(req)) {
      processCors(req, resp, corsConfiguration);
      /*如果是预检请求需要return，以便及时响应预检请求，以便处理后续的真正请求*/
      if (CorsUtils.isPreFlightRequest(req)) {
        return;
      }
    }
    doService(req, resp);
  }

  /**
   * 此方法是真正的请求处理方法，核心的任务有：
   * <ol>
   *     <li>处理HandlerContext</li>
   *     <li>利用HandlerMapping找到HandlerExecutionChain</li>
   *     <li>由doDispatch去处理链的执行</li>
   *     <li>由noHandlerFound去处理找不到Handler（也就没有链）的情况</li>
   * </ol>
   * <p>
   *     注意：HandlerMapping查找Handler的过程中出现的异常不会被HandlerExceptionResolver去处理
   * </p>
   *
   * @param req  请求对象
   * @param resp 响应对象
   */
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    HandlerExecutionChain chain;
    HandlerContext context = HandlerContext.getContext();
    context.setRequest(req)
            .setResponse(resp);
    try {
      chain = getHandler(req);
      if (chain != null) {
        doDispatch(req, resp, chain);
      } else {
        noHandlerFound(req, resp);
      }
    } catch (Throwable ex) {
      /* spring mvc在这个地方是做了额外的异常处理的 */
      System.out.println("可以在这里再做一层异常处理，比如处理视图渲染方面的异常等，但现在什么都没做,异常消息是:" + ex.getMessage());
    } finally {
      /* 保存到ThreadLocal的内容一定要清掉，所以放在finally是合理的 */
      context.clear();
    }
  }

  /**
   * 此方法完成了链的执行和视图结果的渲染
   *
   * @param req   请求对象
   * @param resp  响应对象
   * @param chain 执行链
   * @throws Throwable 整个请求处理过程中可能出现的异常
   */
  protected void doDispatch(HttpServletRequest req, HttpServletResponse resp, HandlerExecutionChain chain) throws Throwable {
    ViewResult viewResult;
    try {
      // 这里返回false，执行完拦截器的后置逻辑后直接return，结束后续流程
      if (!chain.applyPreHandle(req, resp)) {
        chain.applyPostHandle(req, resp);
        return;
      }
      viewResult = applyHandle(req, resp, chain.getHandler());
      chain.applyPostHandle(req, resp);
    } catch (Exception ex) {
      // 拦截器的前置代码或者handler的执行出了异常，已正确执行过前置逻辑的拦截器的后置逻辑即便出了异常也需要执行
      chain.applyPostHandle(req, resp);
      // 这里只处理Exception，非Exception并没有处理，会继续抛出给doService处理.
      viewResult = resolveException(req, resp, chain.getHandler(), ex);
    }
    /*
     * 如果执行链能正常执行，那么viewResult一定不为null，
     * 如果执行链执行异常，但异常解析器能正确的解析，那么viewResult也不会为null
     * 如果执行链异常，异常解析器也 不能 处理异常，那么resolveException方法会抛出此异常，render不会得到执行
     */
    render(req, resp, viewResult);
  }

  protected ViewResult applyHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
    HandlerAdapter adapter = getHandlerAdapter(handler);
    return adapter.handle(req, resp, handler);
  }

  protected ViewResult resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) throws Exception {
    for (HandlerExceptionResolver exceptionResolver : exceptionResolvers) {
      ViewResult result = exceptionResolver.resolveException(req, resp, handler, ex);
      if (result != null) {
        return result;
      }
    }
    /* 表示没有一个异常解析器可以处理异常，那么就应该把异常继续抛出,会交给doService方法去处理，因而也不会进行渲染处理 */
    throw ex;
  }

  /**
   * 这里是对视图结果进行渲染处理，主要是通过调用ViewResult的render方法实现的，具体逻辑见各个ViewResult的子类
   * <p>
   * 这里不需要对viewResult进行null的判断，具体原因见{@link #doDispatch(HttpServletRequest, HttpServletResponse, HandlerExecutionChain)}方法内的注释
   * </p>
   *
   * @param req        请求对象
   * @param resp       响应对象
   * @param viewResult 视图结果
   * @throws Exception 渲染时可能抛出的异常
   */
  protected void render(HttpServletRequest req, HttpServletResponse resp, ViewResult viewResult) throws Exception {
    viewResult.render(req, resp);
  }

  protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    for (HandlerMapping mapping : handlerMappings) {
      HandlerExecutionChain chain = mapping.getHandler(request);
      if (chain != null) {
        return chain;
      }
    }
    return null;
  }

  /**
   * 设置编码的方法是在service方法里面第一个调用，如果已经从req
   * 对象中获取数据了，再设置这个编码是无效的
   *
   * @param req  请求对象
   * @param resp 响应对象
   * @throws Exception 设置编码时可能抛出的IOException
   */
  @SuppressWarnings("JavaDoc")
  protected void setEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

  protected void noHandlerFound(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    // resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        /* 利用default servlet来处理当前请求找不到handler的情况，默认servlet也可以处理静态资源，
        具体见spring mvc的DefaultServletHttpRequestHandler类 */
    req.getServletContext()
            .getNamedDispatcher("default")
            .forward(req, resp);
  }

  protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
    for (HandlerAdapter adapter : handlerAdapters) {
      if (adapter.supports(handler)) {
        return adapter;
      }
    }
    throw new ServletException("此Handler没有对应的adapter去处理，请在DispatcherServlet中进行额外的配置");
  }

  /**
   * 参考spring 的DefaultCorsProcessor
   * <p>在预检请求下，才会设置的项
   * <ul>
   *     <li>setAccessControlMaxAge</li>
   *     <li>setAccessControlAllowHeaders</li>
   *     <li>setAccessControlAllowMethods</li>
   * </ul>
   * </p>
   *
   * <p>
   * 不管是不是预检请求都会设置的项有
   *     <ul>
   *         <li>setAccessControlAllowOrigin</li>
   *         <li>setAccessControlAllowCredentials</li>
   *     </ul>
   * </p>
   *
   * @param req           请求对象
   * @param resp          响应对象
   * @param configuration 跨域配置
   */
  protected void processCors(HttpServletRequest req, HttpServletResponse resp, CorsConfiguration configuration) {
    String requestOrigin = req.getHeader(HttpHeaders.ORIGIN);
    String allowOrigin = configuration.checkOrigin(requestOrigin);
    if (allowOrigin == null) {
      rejectRequest(resp);
      return;
    }
    // 设置允许跨域请求的源
    resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
    resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(configuration.getAllowCredentials()));

    if (HttpMethod.OPTIONS.matches(req.getMethod())) {
      // 浏览器缓存预检请求结果时间,单位:秒
      resp.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, Long.toString(configuration.getMaxAge()));
      // 允许浏览器在预检请求成功之后发送的实际请求方法名，
      // 在MDN中只说要用逗号分隔即可，https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Allow-Methods
      // 但其举的例子是逗号后有一个空格，spring的HttpHeaders类的toCommaDelimitedString也是这样的
      resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, StringUtils.toCommaDelimitedString(configuration.getAllowedMethods(), ", "));
      // 允许浏览器发送的请求消息头
      resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.toCommaDelimitedString(configuration.getAllowedHeaders(), ", "));

    }
  }

  protected void rejectRequest(HttpServletResponse response) {
    try {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getOutputStream()
              .write("无效的跨域请求".getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (IOException e) {
      throw new IllegalStateException("跨域处理失败");
    }
  }
  // endregion
}
