package com.nf.mvc.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解修饰在方法参数上是用来反序列化前端请求体中的数据为参数类型数据用的.
 * <h3>注意事项</h3>
 * <p>
 *     由于是读取请求体中的数据进行反序列化操作，所以当方法参数有多个RequestBody修饰的参数时，
 *     解析第二个参数时就会报流已关闭的错误，比如下面的代码
 *     <pre class="code">
 *         public void doHandle(@RequestBody SomeClass1 sc1,@RequestBody SomeClass2 sc2){}
 *     </pre>
 *     解决办法是把只用一个参数修饰@RequestBody，把参数类型换成Map或者是把类型再包一层，比如
 *     <pre class="code">
 *         &#64;Data
 *          public class SomeVO{
 *              private SomeClass1 sc1;
 *              private SomeClass2 sc2;
 *          }
 *    </pre>
 * </p>
 *
 * <p>第二个要注意的是此注解修饰的参数与上传文件同时进行时，也是不支持的，因为两者都是从请求体中读取数据，
 * 只要有一个读取完了，流就关闭了，下一个再去读时会报流已关闭的错误，碰到既有文件上传，又有数据时，建议像下面这样
 * 用一个单独的类来获取非文件数据
 * <pre class="code">
 *      public void doHandler(MultipartFile file SomeClass sc){}
 * </pre>
 * 或者你可以用String来接收前端传递过来的非文件数据，然后自己对字符串进行反序列化操作，比如:
 * <pre class="code">
 *      public void doHandler(MultipartFile file String data){
 *          //交给业务层去反序列化这个json格式的字符串文本
 *      }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestBody {
}
