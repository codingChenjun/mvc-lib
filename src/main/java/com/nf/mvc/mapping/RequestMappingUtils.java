package com.nf.mvc.mapping;

import java.lang.reflect.AnnotatedElement;

public abstract class RequestMappingUtils {
  /**
   * @param element AnnotatedElement类型代表着所有可以放置注解的元素，比如类，方法参数，字段等
   * @return 返回RequestMapping注解中指定的url模式值
   */
  public static String getUrlPattern(AnnotatedElement element) {
    return element.isAnnotationPresent(RequestMapping.class) ?
            element.getDeclaredAnnotation(RequestMapping.class).value() : "";
  }
}
