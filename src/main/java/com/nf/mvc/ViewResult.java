package com.nf.mvc;

import com.nf.mvc.view.PlainViewResult;
import com.nf.mvc.view.VoidViewResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此类用来封装Handler执行的结果，通常用户的控制器方法会返回此类型，不同的实现通常代表不同的响应情况，
 * 比如用户返回一个{@link com.nf.mvc.view.JsonViewResult},那么就会响应一个json数据给用户。
 * 用户代码中不需要直接实例化某一个具体的ViewResult子类实例，可以借助于{@link com.nf.mvc.handler.HandlerHelper}
 * 类中的静态方法，常见写法如下
 * <pre class="code">
 *  import static com.nf.mvc.handler.HandlerHelper.json;
 *   &#064;RequestMapping("/json")
 *   public ViewResult json(...){
 *       //省略逻辑代码
 *       return json(new ResponseVO(...));
 *   }
 * </pre>
 * @see com.nf.mvc.view.JsonViewResult
 * @see PlainViewResult
 * @see com.nf.mvc.view.ForwardViewResult
 * @see com.nf.mvc.view.RedirectViewResult
 * @see com.nf.mvc.view.HtmlViewResult
 * @see VoidViewResult
 */
public abstract class ViewResult {
    public abstract void render(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    /**
     * 此方法是用来把控制器方法的执行结果统一适配为ViewResult类型用的，适配逻辑是
     * <ul>
     *     <li>控制器方法返回null：比如方法签名为void或就是return null,那么就适配为VoidViewResult</li>
     *     <li>控制器方法返回ViewResult类型：原样返回，无需适配</li>
     *     <li>控制器方法返回非ViewResult类型：那么就把返回对象的toString()值适配为PlainViewResult</li>
     * </ul>
     * <p>这段代码在Adapter与ExceptionResolver里面都有使用，因为Adapter与ExceptionResolver本质上不相关
     * 所以这段代码放在两个里面的任意一个都不合适</p>
     * <p>不放在HandlerHelper里是因为此类主要是用户使用，但此方法用户基本不用，
     * 所以选择把此方法放到ViewResult类里
     * </p>
     * @param handlerResult: handler执行之后的结果，可能是null，ViewResult或者别的类型，详见{@link HandlerAdapter}
     * @return 返回handler方法执行后适配的结果
     */
    public static ViewResult adaptHandlerResult(Object handlerResult) {
        ViewResult viewResult;
        if (handlerResult == null) {
            /* 这种情况表示handler方法执行返回null或者方法的签名本身就是返回void，
             反射调用一个void类型的方法时，invoke方法的返回值是null*/
            viewResult = new VoidViewResult();
        } else if (handlerResult instanceof ViewResult) {
            viewResult = (ViewResult) handlerResult;
        } else {
            viewResult = new PlainViewResult(handlerResult.toString());
        }

        return viewResult;
    }
}
