package com.nf.mvc.support;

/**
 * 拷贝自spring，原本是个抽象类，只保留了一个方法
 * 这里特意改成接口，演示工具类在新jdk用接口实现是很好的
 * 此类型的一些功能在jdk中的{@link java.util.Objects}是有类似的
 */
public interface Assert {
  static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }
}
