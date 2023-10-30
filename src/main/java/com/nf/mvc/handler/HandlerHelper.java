package com.nf.mvc.handler;

import com.nf.mvc.util.StreamUtils;
import com.nf.mvc.view.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 此类都是一些静态方法，主要是给用户编写的控制器方法处理返回值时提供一些便利方法。
 * 通常是在用户编写的控制器类采用java的静态方法导入的形式使用这些便利方法，
 * 典型的使用方法如下：
 * <pre class="code">
 *   import static com.nf.mvc.handler.HandlerHelper.json
 *   public class SomeController{
 *      &#064;RequestMapping("/demo")
 *      public ViewResult json(...){
 *          return json(new ResponseVO(...));
 *      }
 *    }
 * </pre>
 */
public class HandlerHelper {
    public static VoidViewResult empty() {
        return new VoidViewResult();
    }

    public static JsonViewResult json(Object obj) {
        return new JsonViewResult(obj);
    }

    public static PlainViewResult plain(String text) {
        return new PlainViewResult(text);
    }

    public static HtmlViewResult html(String html) {
        return new HtmlViewResult(html);
    }

    public static ForwardViewResult forward(String url) {
        return forward(url, new HashMap<>());
    }

    public static ForwardViewResult forward(String url,Map<String, Object> model) {
        return new ForwardViewResult(url,model);
    }

    public static RedirectViewResult redirect(String url) {
        return redirect(url, new HashMap<>());
    }

    public static RedirectViewResult redirect(String url,Map<String,String> model) {
        return new RedirectViewResult(url,model);
    }

    public static FileViewResult file(String realPath) {
        return file(realPath,new HashMap<>(4));
    }

    public static FileViewResult file(String realPath,Map<String,String> headers) {
        return new FileViewResult(realPath,headers);
    }

    public static FileViewResult file(InputStream inputStream,String filename) {
        return file(inputStream,filename,new HashMap<>(4));
    }

    public static FileViewResult file(InputStream inputStream,String filename,Map<String,String> headers) {
        return new FileViewResult(inputStream,filename,headers);
    }

    public static StreamViewResult stream(InputStream inputStream) {
        return stream(inputStream, new HashMap<>());
    }

    public static StreamViewResult stream(InputStream inputStream, Map<String,String> headers) {
        return new StreamViewResult(inputStream,headers);
    }

    public static StreamViewResult stream(String realPath) {
        return stream(realPath, new HashMap<>(4));
    }

    public static StreamViewResult stream(String realPath, Map<String,String> headers) {
        return new StreamViewResult(StreamUtils.getInputStreamFromRealPath(realPath),headers);
    }
}
