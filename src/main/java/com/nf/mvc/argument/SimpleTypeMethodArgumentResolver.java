package com.nf.mvc.argument;

import com.nf.mvc.support.WebTypeConverters;
import com.nf.mvc.util.ClassUtils;
import com.nf.mvc.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 简单类型的参数解析器，简单类型基本就是八大基本类型及其包装类型加上一些时间相关的类型，具体见{@link ClassUtils#isSimpleType(Class)}
 * <p>
 * 简单类型主要基于参数名来获取数据，你也可以通过注解@RequestParam来指定别的名字来获取数据，
 * 如果从请求端获取不到数据，就采用注解@RequestParam指定的默认值
 * </p>
 *
 * @see ReflectionUtils
 * @see WebTypeConverters
 */
public class SimpleTypeMethodArgumentResolver extends AbstractCommonTypeMethodArgumentResolver {
  @Override
  protected boolean supportsInternal(Class<?> scalarType) {
    return ClassUtils.isSimpleType(scalarType);
  }

  @Override
  protected Object resolveScalarType(Class<?> scalarType, Object parameterValue, MethodParameter methodParameter) throws Exception {
    Object value = parameterValue;
    // 请求端没有传递数据，就去看注解指定的默认值
    if (value == null) {
      value = methodParameter.getDefaultValue();
    }
    // 如果值还是null并且参数类型是简单类型，简单类型不能赋值为null，所以抛异常
    if (value == null && ClassUtils.isPrimitive(scalarType)) {
      throw new IllegalArgumentException("参数名:" + methodParameter.getParameterName() + " 的值为null，不能把null给简单类型:" + scalarType);
    }
    // 值不为null才进行类型转换，这个不为null的值可能是从请求端获取的，也可能是默认值提供的。注解提供的默认值也是需要进行类型转换的
    // 转换是可能失败的，比如把字符串abc值转换为整数，所以这里是可能抛异常的
    if (value != null) {
      value = WebTypeConverters.convert(scalarType, value.toString());
    }
    // 这里返回的可能是null或者类型转换成功后的数据,value是null赋值给非基本类型是没有问题的
    return value;
  }

  @Override
  protected Object[] getSource(MethodParameter methodParameter, HttpServletRequest request) {
    return request.getParameterValues(methodParameter.getParameterName());
  }

}
