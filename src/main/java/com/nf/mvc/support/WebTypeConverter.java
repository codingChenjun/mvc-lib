package com.nf.mvc.support;

import com.nf.mvc.argument.MethodParameter;
import com.nf.mvc.argument.SimpleTypeMethodArgumentResolver;
import com.nf.mvc.util.ClassUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 类型转换器接口，主要是把web中的String数据类型转换为简单类型,简单类型定义见{@link ClassUtils#isSimpleType(Class)}
 * 这些转换器通常被参数解析器使用(见：{@link SimpleTypeMethodArgumentResolver#resolveArgument(MethodParameter, HttpServletRequest)})
 * <p>在此接口的实现类中,不需要对参数值的null进行特别的处理,只要转换不了直接抛异常即可.
 * 只有参数解析器会基于某些规则对null值进行一些特别的处理,这个职责不是{@code  WebTypeConverter}该做的</p>
 * <p>在设计时你可以不把类设计为泛型类,而只是把方法设计为泛型方法,比如{@code <T> T convert(String paramValue)},但这样设计的话,
 * 在实现类编写实现方法时,比如:{@code BigDecimal convert(String paramValue}会报一个警告,说需要的是T,但此刻是BigDecimal,建议把类泛型化</p>
 *
 * @see WebTypeConverters
 * @see SimpleTypeMethodArgumentResolver
 */
public interface WebTypeConverter<T> {
  T convert(String paramValue) throws Exception;
}