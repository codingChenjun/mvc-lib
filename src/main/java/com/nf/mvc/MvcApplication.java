package com.nf.mvc;


import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.MultipartConfigElement;
import java.time.LocalTime;

/**
 * 嵌入式Tomcat启动类，这点是模仿spring boot的行为来实现的,可以让mvc框架直接通过入口函数的方式来启动
 * <h3>基本使用</h3>
 * 编写类似下面这样的代码，直接运行就可以启动mvc项目
 * <pre class="code">
 *     public static void main(String[] args) {
 *         MvcApplication.run(args);
 *     }
 * </pre>
 * <h3>参数设定</h3>
 * 默认情况下，有如下默认值
 * <ul>
 *     <li>contextPath=/mvc</li>
 *     <li>port=8080</li>
 *     <li>basePackage=mvc</li>
 *     <li>urlPattern=/</li>
 *     <li>文件上传时使用的临时目录=System.getProperty("java.io.tmpdir")</li>
 * </ul>
 * <h3>参考资料</h3>
 * <a href="https://devcenter.heroku.com/articles/create-a-java-web-application-using-embedded-tomcat">嵌入式tomcat</a>
 * <a href="https://www.cnblogs.com/develon/p/11602969.html">嵌入式tomcat以及集成spring</a>
 * <a href="https://www.cnblogs.com/pilihaotian/p/8822926.html">spring boot 处理jsp的分析</a>
 * <a href="https://www.cnblogs.com/lihw-study/p/17281721.html">嵌入式tomcat添加default servlet</a>
 * <a href="https://stackoverflow.com/questions/16239130/java-user-dir-property-what-exactly-does-it-mean">解释了user.dir的含义以及其它各种属性含义的链接</a>
 * <i>此类的编写参考了spring boot中的TomcatServletWebServerFactory中的代码(重点是getWebServer方法)</i>
 * @author cj
 */
public class MvcApplication {
    public static final String CONTEXT_PATH = "contextPath";
    public static final String PORT = "port";
    public static final String BASE_PACKAGE = "basePackage";
    public static final String URL_PATTERN = "urlPattern";

    /**
     * 上下文路径必须是空字符串或者以字符"/"开头但不以字符"/"结尾
     */
    private static final String CONTEXT_PATH_DEFAULT = "";
    private static final int PORT_DEFAULT = 8080;
    private static final String BASE_PACKAGE_DEFAULT = "mvc";
    private static final String URL_PATTERN_DEFAULT = "/";
    private static final String TEMP_DIR_DEFAULT = System.getProperty("java.io.tmpdir");


    private  String contextPath ;
    private int port;
    private  String basePackage;
    private  String urlPattern;


    public static void run(String... args) {
        new MvcApplication().start(args);
    }

    private void start(String... args) {
        parseArgs(args);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        //等价于new File(".").getAbsolutePath();
        String docBase = System.getProperty("user.dir");
        Context ctx = registerContext(tomcat, docBase);
        //这行代码也会注册默认servlet,细节见https://stackoverflow.com/questions/6349472/embedded-tomcat-not-serving-static-content
        Tomcat.initWebappDefaults(ctx);
        //registerDefaultServlet(ctx);
        registerDispatcherServlet(ctx);
        registerShutdownHook(tomcat);

        startEmbeddedTomcat(tomcat);

    }

    private void parseArgs(String... args) {
        // 先赋值为默认值
        setDefaultValues();

        //解析参数中设定的值
        for (String arg : args) {
            String[] argument = arg.split("=");
            String key = argument[0];
            String value = argument[1];
            if (CONTEXT_PATH.equalsIgnoreCase(key)) {
                contextPath = value;
            }
            if (PORT.equalsIgnoreCase(key)) {
                port = Integer.parseInt(value);
            }
            if (BASE_PACKAGE.equalsIgnoreCase(key)) {
                basePackage = value;
            }
            if (URL_PATTERN.equalsIgnoreCase(key)) {
                urlPattern = value;
            }
        }
    }

    private void setDefaultValues() {
        contextPath = CONTEXT_PATH_DEFAULT;
        port = PORT_DEFAULT;
        basePackage = BASE_PACKAGE_DEFAULT;
        urlPattern = URL_PATTERN_DEFAULT;
    }

    private void startEmbeddedTomcat(Tomcat tomcat) {
        try {
            tomcat.start();
            printBanner();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private Context registerContext(Tomcat tomcat, String docBase) {
        // 在这篇文章里有说明addContext与addWebApp的区别https://stackoverflow.com/questions/67253024/tomcat-catalina-context-add-existing-servlet-to-context
        // 如果仅仅只是把addContext方法换成addWebApp方法会导致DispatcherServlet注册url-pattern为/失效
        return tomcat.addContext(contextPath, docBase);
    }

    /**
     * 使用这个方法而不使用Tomcat.initWebappDefaults(ctx)可以剔除掉pom中一些关于jsp的相关依赖,
     * Tomcat.initWebappDefaults(ctx)这行代码除了注册默认servlet,还有欢迎页等其他常见的默认初始化设置
     * @param ctx Tomcat的Context
     */
    private void registerDefaultServlet(Context ctx) {
        Tomcat.addServlet(ctx,"default", DefaultServlet.class.getTypeName());
        ctx.addServletMappingDecoded("/", "default");
    }

    private void registerDispatcherServlet(Context ctx) {
        Wrapper wrapper = Tomcat.addServlet(ctx, "dispatcherServlet", new DispatcherServlet());
        ctx.addServletMappingDecoded(urlPattern, "dispatcherServlet");

        wrapper.addInitParameter("base-package", basePackage);
        //这行代码是让DispatcherServlet能支持servlet 3.0标准的文件上传能力
        wrapper.setMultipartConfigElement(new MultipartConfigElement(TEMP_DIR_DEFAULT));
        wrapper.setLoadOnStartup(1);
    }

    private void registerShutdownHook(Tomcat tomcat) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        tomcat.stop();
                        tomcat.destroy();
                    } catch (LifecycleException e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    private void printBanner() {
        //ascii 文本生成是借助于https://tools.kalvinbg.cn/txt/ascii提供的工具生成(采用starwars字体)
        String bannerText = "  ______  __    __   _______ .__   __.        __   __    __  .__   __. \n" +
                " /      ||  |  |  | |   ____||  \\ |  |       |  | |  |  |  | |  \\ |  | \n" +
                "|  ,----'|  |__|  | |  |__   |   \\|  |       |  | |  |  |  | |   \\|  | \n" +
                "|  |     |   __   | |   __|  |  . `  | .--.  |  | |  |  |  | |  . `  | \n" +
                "|  `----.|  |  |  | |  |____ |  |\\   | |  `--'  | |  `--'  | |  |\\   | \n" +
                " \\______||__|  |__| |_______||__| \\__|  \\______/   \\______/  |__| \\__| \n" +
                "                                                                       ";
        System.out.println(bannerText);
        System.out.println("=======================================================================");
        System.out.println("Mvc项目启动成功了:" + LocalTime.now());
        System.out.println(PORT + ":" + this.port);
        System.out.println(CONTEXT_PATH + ":" + this.contextPath);
        System.out.println(URL_PATTERN + ":" + this.urlPattern);
        System.out.println(BASE_PACKAGE + ":" + this.basePackage);
        System.out.println("web资源路径:"+ System.getProperty("user.dir"));
        System.out.println("文件上传用的临时目录:" + ":" + TEMP_DIR_DEFAULT);
        System.out.println("=======================================================================");
    }
}
