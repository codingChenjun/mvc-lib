package com.nf.mvc.configuration;

import com.nf.mvc.DispatcherServlet;
import com.nf.mvc.ioc.Injected;

import javax.servlet.ServletConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * <h3>设计思路</h3>
 * 在整个mvc框架已经完成之后,自己有点想额外添加嵌入式tomcat与yml功能到框架中以模仿spring boot的功能,
 * 所以就引入了{@link com.nf.mvc.MvcApplication },{@link EnableConfigurationProperties},
 * {@link ConfigurationProperties},{@link YmlParser}等类型
 *
 * <h4>主要目的</h4>
 * <p>想把spring boot的读取yml配置文件的特性引入到本mvc框架中来,使用方式也与spring boot类似</p>
 * 并且打算让配置文件只有一个,位置与名字都是固定的,位置就是在类路径下,名字固定是application.yml
 * EnableConfigurationProperties注解修饰在启动类上,用来指定哪些类是配置属性类,
 * 配置属性类是修饰了ConfigurationProperties注解的类型,此类型的属性来自于yml配置文件,
 * 要使用这些配置类的类型,在其中声明一个字段并在其上加入Injected注解,表明需要注入配置属性类.
 *
 * <h4>配置属性类的解析时机</h4>
 * 由于MvcApplication的run方法也有可以使用配置属性类的地方,比如读取port设置,如果在这里解析这些配置属性类,
 * 由于此时此刻DispatcherServlet还没有初始化,MvcContext类型中是没有任何类型的,也包括配置属性类,所以放在这里不合适,
 * 除非把扫描类的逻辑进行改写,这可能也影响DispatcherServlet的编写,MvcApplication是最后才添加的,不想这么大动干戈,
 * 所以,要么放弃在run方法里解析配置属性类,要么像spring cloud一样,引入一个bootstrap.yml文件专门用在这种启动mvc项目的方式上,
 * 然后application.yml文件放置在DispatcherServlet的init里进行解析
 *
 * <p><i>
 * 最后的决定:由于教学的目的,最后MvcApplication类放弃对yml文件的处理,配置信息通过命令行参数的方式进行调整.
 * 而配置类的解析处理放在{@link DispatcherServlet#init}初始化阶段处理</i></p>
 *
 * <h4>解析yml文件</h4>
 * 决定了在{@link DispatcherServlet#init}方法进行配置属性的处理之后,我就在initMvcContext时解析并实例化了配置属性类,
 * 并放置到了MvcContext里.之后我在DispatcherServlet里编写解析里,也想像其它Mvc组件一样,分成定制与框架提供这两种不同的配置属性类,
 * 之后我再想到Mvc框架已经通过MvcConfigurer提供了对框架中的一些组件进行配置,而配置属性类某种程度上与MvcConfigurer功能有点类似.
 * 最后,我放弃了mvc框架内部出现配置属性类,只提供对定制配置属性类的处理
 *
 * <h4>哪些类型可以使用配置属性类?</h4>
 * 首先,能使用的类型的实例肯定是被mvc管理的或者是可以被mvc框架获取到的,其次,也牵涉到使用的类型是单例还是原型的.
 * 因为是原型实例的话,这些实例注入配置属性类就不能在{@link com.nf.mvc.DispatcherServlet#init(ServletConfig)}中实现,
 * 因为它只会给一个实例注入一次(init只执行一次),原型实例不能在{@link com.nf.mvc.DispatcherServlet#init(ServletConfig)}
 * 中注入配置属性类就只能在{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}这里进行注入处理,因为整个mvc框架中,
 * 所有被框架管理的类型实例都是靠{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}来实例化的.
 *
 * <p>所以,我最终选择在{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}这里进行配置属性类的注入处理,
 * 这样就不用理会其是否是单例还是原型的问题.</p>
 * <p>当决定{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}在这里进行处理时,又想到有些类的实例化就是
 * 在初始化的时候就实例化了,见{@link com.nf.mvc.MvcContext#resolveMvcClass(Class, Class, List)},
 * 这样的话,如果非要在{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}这里进行注入处理,就必须让配置类先解析处理完毕</p>
 * <p><i>
 * 最后的决定:在{@link com.nf.mvc.MvcContext#resolveMvcClass(Class, Class, List)}进行配置类解析处理,
 * 并且 <b>必须</b>先处理这个工作之后,再去干别的事情,但yml文件解析工作倒是可以放在{@link DispatcherServlet#init}处理,
 * 这个看代码实现时的具体考虑,而且心理一定要清醒,在利用{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}
 * 对配置属性类实例化时是不需要进行注入处理的,最好不用{@link com.nf.mvc.util.ReflectionUtils#newInstance(Class)}它来对
 * 配置属性类进行实例化处理</i></p>
 *
 * <p>整个思考过程,深感没有一个容器功能来管理所有类型的不便,这也没有办法,这个框架本来只是一个教学用的Mvc框架,不能搞太多功能</p>
 * <h4>{@link EnableConfigurationProperties}用在哪里?</h4>
 * 此类本来适合放在入口类上,但因为放弃了,所以此注解也就失去了用武之地,就适合放设计思路了^_^^_^
 * 暂定此注解只能修饰在入口函数main所在的类上面，用来指定哪些类型是一个配置属性类，
 * 可以同时指定多个配置属性类
 * <h3>典型用法</h3>
 * <ul>
 *     <li>在入口类上添加注解{@link EnableConfigurationProperties}指定有哪些配置属性类</li>
 *     <li>编写配置属性类，并在此类上指定注解{@link ConfigurationProperties},并指定前缀，配置文件中对应前缀下的配置项会赋值给本类的相关属性</li>
 *     <li>在要使用配置属性的类中，声明一个类型为配置属性类的字段，并在其上添加注解{@link Injected}</li>
 *     <li>有了这个字段后，就可以调用其属性来获取从application.yml中获取的配置了</li>
 * </ul>
 * 示例代码如下:
 * <pre class="code">
 *
 * &#064;EnableConfigurationProperties({MyConfigurationProperties})
 * public class MyApplication{
 *     public static void main(String[] args) {
 *
 *     }
 * }
 * &#064;ConfigurationProperties("server")
 * public class MyConfigurationProperties{
 *     private int port;
 *     private String path;
 *     //getter setter...
 * }
 *
 * public class SomeClass {
 *     &#064;Autowired
 *     private MyConfigurationProperties config;
 *     public void doSth(){
 *         int port = config.getPort();
 *         String path = config.getPath();
 *     }
 * }
 * yml配置如下:
 * server:
 *      port: 8000
 *      path: /somePath
 *
 * </pre>
 * <h3>配置属性类</h3>
 * 配置属性类是修饰了{@link ConfigurationProperties}注解的类型，此类型都是一些简单的属性，
 * 这些属性值都是来自于项目的application.yml文件的配置信息
 * <h3>使用限制</h3>
 * <p>由于本框架不是一个类似于spring的容器框架，不是所有与的类型都是被框架所管理的，
 * 所以那些不被框架框里的类型是无法注入配置属性的，切记！
 * </p>
 * <h3>参考资料</h3>
 * <a href="https://blog.csdn.net/m0_37556444/article/details/113513089">推断main函数所在的类（来自于spring boot)</a>
 * <a href="https://www.codenong.com/11306811/">获取方法调用者所在的类</a>
 *
 * @author cj
 * @see com.nf.mvc.MvcContext
 * @see ConfigurationProperties
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableConfigurationProperties {
  Class<?>[] value() default {};
}
