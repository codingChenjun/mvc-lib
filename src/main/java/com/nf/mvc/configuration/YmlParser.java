package com.nf.mvc.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static com.nf.mvc.util.ReflectionUtils.setFieldValue;
import static com.nf.mvc.util.StringUtils.hasText;

/**
 * 此类是用来解析yml文件用的,不提供文件名的话,默认是解析类路径下的application.yml文件.
 * <p>此类只解析了map结构的数据,并把这个map结构解析成一个bean对象,并没有对其它情况进行处理,
 * 比如yml中的list类型等</p>
 * 此类参考了网上的代码并稍作修改,并没有严格的测试
 * <h3>基本使用</h3>
 * 假定你有一个如下的配置属性类
 * <pre class="code">
 *   &#064;ConfigurationProperties("s1")
 * public class MyConfigurationProperties1 {
 *     private int id;
 *     private String name;
 *     //省略掉getter,setter
 * }
 * </pre>
 * 那么你在yml文件中配置内容如下就可以顺利把yml中的数据赋值给配置属性类了
 * <pre class="code">
 *    s1:
 *      id: 100
 *      name: abc
 * </pre>
 * 解析器代码使用方法如下
 * <pre class="code">
 *   MyConfigurationProperties1 s1 = YmlParser.getInstance().parse("s1", MyConfigurationProperties1.class);
 * </pre>
 * <h3>参考资料</h3>
 * <a href="https://juejin.cn/post/7034489501284040711">yaml解析工具类</a>
 * <a href="https://www.baeldung.com/java-snake-yaml">用snakeYml库解析yml文件 </a>
 *
 * @author cj
 */
public class YmlParser {
  private static final String DEFAULT_CONFIG_FILE = "application.yml";
  private volatile static YmlParser instance;
  private boolean fileLoaded = false;
  /**
   * 解析yml配置文件后获得的顶层map结构
   */
  private Map<?,?> origin;
  /**
   * 获取的对象
   */
  private Map<?,?> current;

  private YmlParser() {
  }
  public static YmlParser getInstance() {
    return getInstance(DEFAULT_CONFIG_FILE);
  }

  public static YmlParser getInstance(String fileName) {
    if (instance == null) {
      synchronized (YmlParser.class) {
        if (instance == null) {
          instance = new YmlParser();
          instance.load(fileName);
        }
      }
    }
    return instance;
  }

  /**
   * 依据层级关系进行解析,如果没有配置文件并不会抛异常,仅仅只会返回null,这样配置属性类就是一个null值
   *
   * @param prefix 配置的前缀,用句号(.)表示层级关系
   * @return 解析器本身, 便于链式调用
   */
  public <T> T parse(String prefix, Class<T> configurationPropertiesCLass) {
    if (!isFileLoaded() || !hasText(prefix)) {
      return null;
    }
    //每次要解析之前先恢复
    reset();
    //获取层级关系
    String[] keys = prefix.trim().split("\\.");
    for (String key : keys) {
      //只对map类型进行了处理,没有处理current是其它类型的情况
      this.current = (Map<?,?>) (this.current.get(key));
    }
    return populateBean(configurationPropertiesCLass, this.current);
  }

  /**
   * 加载配置文件
   */
  private void load(String fileName) {
    InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(fileName);
    if (inputStream != null) {
      this.fileLoaded = true;
      Yaml yaml = new Yaml();
      this.origin = yaml.load(inputStream);
      this.current = this.origin;
    }
  }

  private boolean isFileLoaded() {
    return fileLoaded;
  }

  private void reset() {
    this.current = this.origin;
  }

  /**
   * @param clazz 配置类的class
   * @param <T> 泛型形参
   * @return 填充了数据之后的配置类对象
   */
  private <T> T populateBean(Class<T> clazz, Map<?,?> map) {
    T obj;
    try {
      //实例化配置属性类,不能使用ReflectionUtils.newInstance,因为它还有一个作用就是用来注入配置属性类的
      obj = clazz.newInstance();
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        setFieldValue(obj,field,map.get(field.getName()));
      }
    } catch (Exception e) {
      throw new RuntimeException("配置属性类注入失败,是否是配置属性没有默认构造函数?",e);
    }
    return obj;
  }
}