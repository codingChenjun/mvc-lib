package com.nf.mvc.exception;

import com.nf.mvc.HandlerExceptionResolver;
import com.nf.mvc.MvcContext;
import com.nf.mvc.ViewResult;
import com.nf.mvc.handler.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.nf.mvc.ViewResult.adaptHandlerResult;
import static com.nf.mvc.util.AnnotationUtils.getAttrValue;
import static com.nf.mvc.util.ExceptionUtils.exceptionCompare;
import static com.nf.mvc.util.ExceptionUtils.getRootCause;

/**
 * 这是mvc框架主用的一个异常解析器，此异常解析器支持用ExceptionHandler注解.
 * <h3>典型用法</h3>
 * <p>
 * 用户通常编写一个单独的类，此类里面每一个方法用来处理某一个异常，代码如下：
 * <pre class="code">
 *      public class GlobalExceptionHandler{
 *          &#064;ExceptionHandler(RuntimeException.class)
 *          public JsonViewResult handleRuntime(RuntimeException re){
 *               System.out.println("re = " + re);
 *              return new JsonViewResult(new ResponseVO(10001,"订单超时。。","runtime--"));
 *          }
 *
 *          &#064;ExceptionHandler(ArithmeticException.class)
 *          public JsonViewResult handleArithmeticException(ArithmeticException re){
 *              System.out.println("suan shu re = " + re);
 *              return new JsonViewResult(new ResponseVO(10002,"某个描述","算数--"));
 *          }
 *      }
 *  </pre>
 * </p>
 *
 * <p>
 * 如果一个异常有多个异常处理方法都是可以处理的，那么会用ExceptionHandler注解中指定的能处理类型最接近(指的是继承链条上)引发的异常的方法来处理异常，
 * 比如引发了一个ArithmeticException，那么上面的示例代码中handleArithmeticException方法会被用来处理异常，
 * 如果类中没有能处理ArithmeticException异常的方法，那么就会交给handleRuntime方法去处理
 * </p>
 * <p>
 * 这些异常处理方法必须有一个参数，并且类型只能是Exception类型或其子类型，
 * 并不能像Handler那样支持各种类型，你如果有兴趣可以改写此类的实现来达成此目的
 * 其方法返回值与handler的方法返回值是一样的，可以是void，ViewResult或其它类型，
 * 异常解析器负责把异常处理方法的返回结果适配为ViewResult类型
 * </p>
 * <h3>核心思想</h3>
 * <p>
 *   <ul>
 *     <li>找到异常处理方法:{@link #resolveExceptionHandlerMethods()}
 *          <ol>
 *              <li>实例化时扫描所有符合规则的异常处理方法，见{@link #scanExceptionHandlerMethods(Predicate)}</li>
 *              <li>如果要对第一步扫描到的方法进行额外的后置处理就执行{@link #postHandleExceptionHandlerMethods(List)}方法</li>
 *           </ol>
 *     </li>
 *     <li>处理用户引发的异常:{@link #resolveException(HttpServletRequest, HttpServletResponse, Object, Exception)} ()}
 *          <ol>
 *            <li>找出真正的异常:{@link #getRaisedException(Exception)} </li>
 *            <li>找出最匹配的异常处理方法：{@link #findMostMatchedHandlerMethod(List, Exception)} </li>
 *            <li>执行异常处理方法以处理用户引发的异常:{@link #executeExceptionHandlerMethod(HandlerMethod, Exception, HttpServletRequest)}</li>
 *          </ol>
 *     </li>
 *   </ul>
 *   <h3>扩展建议</h3>
 *   子类通常只需要重写这5个方法：{@link #scanExceptionHandlerMethods(Predicate)},{@link #postHandleExceptionHandlerMethods(List)},
 *   {@link #getRaisedException(Exception)},{@link #findMostMatchedHandlerMethod(List, Exception)},{@link #executeExceptionHandlerMethod(HandlerMethod, Exception, HttpServletRequest)},
 *   而不建议重写{@link #resolveExceptionHandlerMethods()}与{@link #resolveException(HttpServletRequest, HttpServletResponse, Object, Exception)} ()},
 *   所以这两个方法可以设置为final了,我这里只设置了{@link #resolveException(HttpServletRequest, HttpServletResponse, Object, Exception)}为final
 * </p>
 * <h3>线程安全性</h3>
 * <p>此类是一个单例对象，并且在{@link com.nf.mvc.DispatcherServlet}类的初始化阶段就已经完成实例化，
 * 所以，此类对异常处理方法的解析只有一次，后续在多线程环境下对方法{@link #resolveException(HttpServletRequest, HttpServletResponse, Object, Exception)}
 * 也不会存在线程不安全的问题</p>
 *
 * @see com.nf.mvc.DispatcherServlet
 * @see HandlerExceptionResolver
 * @see ExceptionHandler
 * @see ExceptionHandlersExceptionResolver
 */
public class ExceptionHandlerExceptionResolver implements HandlerExceptionResolver {
  /**
   * 子类不要直接访问此字段，通过对应的getter方法来访问此字段，
   * 这里用HandlerMethod类型而不用Method类型是为了以后可能的功能增强，比如支持的参数与Handler一致(RequestMappingHandlerAdapter 处理的handler就是HandlerMethod)，
   * 而不是像此类实现一样只支持一个Exception类型的参数
   */
  private final List<HandlerMethod> exceptionHandlerMethods = new ArrayList<>();

  public ExceptionHandlerExceptionResolver() {
    resolveExceptionHandlerMethods();
  }

  /**
   * 这个方法就是用来找到异常处理方法以及对他们对找到的这些异常处理方法进行后置处理,通常的后置处理是对这些异常处理方法基于其能处理的异常进行排序处理.
   * <p>比如有下面的两个异常处理方法,都能对ArithmeticException异常进行处理,那么更合适的就是m1,进行后置处理之后,m1就排在m2的前面
   * <pre class="code">
   *    &#064;ExceptionHandler(ArithmeticException ex)
   *    m1(ArithmeticException ex){},
   *
   *    &#064;ExceptionHandler( RuntimeException ex)
   *    m2(RuntimeException ex){}
   * </pre>
   * </p>
   * <p>
   * 这个方法被设计成protected，是为了可以被继承，以便改写ExceptionHandlerExceptionResolver的异常处理逻辑，
   * 比如支持其它的注解或者有其它的排序算法等。给scanExceptionHandlerMethods与方法sortExceptionHandleMethods设计为有参数的形式
   * 是为了增加灵活性，比如下面的重写方法就改变成了方法上有ExHandler注解的才是异常处理方法
   * <pre class="code">
   *         &#064;Override
   *         protected void resolveExceptionHandlerMethods(){
   *             scanExceptionHandlerMethods(method->method.isAnnotationPresent(ExHandler.class));
   *             sortExceptionHandleMethods(this.exHandleMethods);
   *         }
   *      </pre>
   * 而如果重写了scanExceptionHandlerMethods方法，那么可以完全改写扫描获取异常处理的逻辑，具有更大的灵活性,
   * 子类可以通过调用方法{@link #getExceptionHandlerMethods()}来获取所有的异常处理方法的集合类型,
   * 这样子类在编写自定义异常方法扫描逻辑时就可以把找到的处理方法添加到这个集合中，以便后续的方法使用，比如handleExceptionHandlerMethods
   * </p>
   */
  protected void resolveExceptionHandlerMethods() {
    scanExceptionHandlerMethods(method -> method.isAnnotationPresent(ExceptionHandler.class));
    postHandleExceptionHandlerMethods(this.exceptionHandlerMethods);
  }

  /**
   * 此方法解析出所有的异常处理方法，
   *
   * @param predicate:判断条件，此谓词返回true的才是有效的异常处理方法
   */
  protected void scanExceptionHandlerMethods(Predicate<Method> predicate) {
    List<Class<?>> classList = MvcContext.getMvcContext()
            .getAllScannedClasses();

    for (Class<?> clz : classList) {
      Method[] methods = clz.getDeclaredMethods();
      for (Method method : methods) {
        if (predicate.test(method)) {
          HandlerMethod exHandleMethod = new HandlerMethod(method);
          exceptionHandlerMethods.add(exHandleMethod);
        }
      }
    }
  }

  /**
   * 这个方法是用来对所有的异常处理方法进行后置处理用的，常见的处理是对异常处理方法基于其能处理的异常进行排序，
   * 也可以不对这些异常处理方法进行任何额外的处理，子类可以通过重写此方法并留空或者重写{@link ExceptionHandlerExceptionResolver#resolveExceptionHandlerMethods()}
   * 但只调用{@link ExceptionHandlerExceptionResolver#scanExceptionHandlerMethods(Predicate)}的方式来达成不对异常处理方法进行额外处理的效果
   *
   * @param exHandlerMethods 异常处理方法集合
   */
  protected void postHandleExceptionHandlerMethods(List<HandlerMethod> exHandlerMethods) {
    exHandlerMethods.sort((m1, m2) -> exceptionCompare(
            getAttrValue(m1.getMethod(), ExceptionHandler.class),
            getAttrValue(m2.getMethod(), ExceptionHandler.class)));
  }


  @Override
  public final ViewResult resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Exception raisedException = getRaisedException(ex);
    HandlerMethod exceptionHandlerMethod = findMostMatchedHandlerMethod(getExceptionHandlerMethods(), raisedException);
    if (exceptionHandlerMethod == null) {
      return null;
    }
    try {
      Object exceptionHandlerResult = executeExceptionHandlerMethod(exceptionHandlerMethod, raisedException, request);
      return adaptHandlerResult(exceptionHandlerResult);
    } catch (Exception e) {
      /* 进入到这里就是异常处理方法本身的执行出了错，catch里如果什么都不干，相当于吞掉异常处理方法本身的异常;
       异常处理方法本身执行出问题其含义就是说本异常解析器无法处理异常.因此，通过在catch这里返回null的形式，
       就继续交给下一个异常解析器去处理，下一个异常解析器处理的仍然是最开始抛出的异常，也就是这个方法被调用时传递进来的第四个参数的值 */
      return null;
    }
    // return executeExceptionHandlerMethod(exceptionHandlerMethod,raisedException);
  }

  protected Exception getRaisedException(Exception ex) {
    return (Exception) getRootCause(ex);
  }

  /**
   * 此方法是用来找出能对当前用户引发异常进行处理的最合适的异常处理方法,通常是在{@link ExceptionHandlerExceptionResolver#postHandleExceptionHandlerMethods(List)}
   * 方法调用之后才执行本方法,比如{@link ExceptionHandlerExceptionResolver#postHandleExceptionHandlerMethods(List)}已经依据其能处理的异常进行了排序,
   * 那么本方法的实现只只需要找到第一个异常处理方法就可以了,不需要再有额外的逻辑处理,下面的方法就是这样实现的
   *
   * @param handlerMethods 异常处理方法的集合
   * @param exception      当前用户代码引发的异常
   * @return 找到的能处理当前用户异常的HandlerMethod
   * @author cj
   */
  protected HandlerMethod findMostMatchedHandlerMethod(List<HandlerMethod> handlerMethods, Exception exception) {
    HandlerMethod matchedHandlerMethod = null;
    for (HandlerMethod handlerMethod : handlerMethods) {
      Method method = handlerMethod.getMethod();
      Class<?> exceptionClass = method.getDeclaredAnnotation(ExceptionHandler.class)
              .value();
      if (exceptionClass.isAssignableFrom(exception.getClass())) {
        matchedHandlerMethod = handlerMethod;
        break;
      }
    }
    return matchedHandlerMethod;
  }

  protected Object executeExceptionHandlerMethod(HandlerMethod exceptionHandlerMethod, Exception raisedException, HttpServletRequest request) throws Exception {
    Method method = exceptionHandlerMethod.getMethod();
    Object instance = exceptionHandlerMethod.getHandlerObject();
    return method.invoke(instance, raisedException);
  }

  protected List<HandlerMethod> getExceptionHandlerMethods() {
    return exceptionHandlerMethods;
  }
}
