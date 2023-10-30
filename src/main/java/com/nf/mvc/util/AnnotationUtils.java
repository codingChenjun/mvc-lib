package com.nf.mvc.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public abstract class AnnotationUtils {
  public static <T> T getAttrValue(AnnotatedElement ele, Class<? extends Annotation> annoClass) {
    return getAttrValue(ele, annoClass, "value");
  }

  public static <T> T getAttrValue(AnnotatedElement ele, Class<? extends Annotation> annoClass, String attrName) {
    if (!ele.isAnnotationPresent(annoClass)) {
      throw new IllegalStateException("元素:" + ele + " 上没有注解:" + annoClass.getName());
    }
    return getAttributeValue(ele, annoClass, attrName);
  }

  private static <T> T getAttributeValue(AnnotatedElement ele, Class<? extends Annotation> annoClass, String attrName) {
    Annotation anno = ele.getDeclaredAnnotation(annoClass);
    T result;
    try {
      Method valueMethod = annoClass.getDeclaredMethod(attrName);
      result = (T) valueMethod.invoke(anno);
    } catch (Exception e) {
      throw new IllegalStateException("获取注解:" + annoClass.getName() + " 的属性:" + attrName + "值失败", e);
    }
    return result;
  }
}
