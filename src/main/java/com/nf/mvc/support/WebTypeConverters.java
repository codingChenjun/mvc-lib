package com.nf.mvc.support;

import com.nf.mvc.argument.MethodArgumentResolverComposite;
import com.nf.mvc.support.converter.BigDecimalTypeConverter;
import com.nf.mvc.support.converter.BooleanTypeConverter;
import com.nf.mvc.support.converter.ByteTypeConverter;
import com.nf.mvc.support.converter.CharacterTypeConverter;
import com.nf.mvc.support.converter.DateTypeConverter;
import com.nf.mvc.support.converter.DoubleTypeConverter;
import com.nf.mvc.support.converter.FloatTypeConverter;
import com.nf.mvc.support.converter.IntegerTypeConverter;
import com.nf.mvc.support.converter.LocalDateTimeTypeConverter;
import com.nf.mvc.support.converter.LocalDateTypeConverter;
import com.nf.mvc.support.converter.LocalTimeTypeConverter;
import com.nf.mvc.support.converter.LongTypeConverter;
import com.nf.mvc.support.converter.ShortTypeConverter;
import com.nf.mvc.support.converter.StringTypeConverter;
import com.nf.mvc.util.ClassUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WebTypeConverters {
  /**
   * 这是一个最简单的缓存实现，因为静态变量在类加载时处理，一般只加载一次，静态代码块执行之后，这个map内容就不变了
   * 这块内容一直放置在内存中，所以从map中取值时，一直直接在内存中，可以“理解”为从缓存中取
   * <p>
   * web里面的数据都是字符串类型的，要转换的类型也就那么一些固定的简单类型（simpleType），可以采用这种写死的方式，因为没有变化
   * 如果要让mvc框架支持其它新的特定类型，比如MultipartFile这样你自己创造的新类型，那么你就直接添加新的MethodArgumentResolver实现即可
   * 设置一个初始的容量，可以避免一些不必要的多次扩容
   * </p>
   *
   * <p>此类在静态代码块初始化的一些转换器要尽量与{@link ClassUtils#isSimpleType(Class)}
   * 方法对简单类型的定义匹配</p>
   * <p>此类有一个简单的缓存实现，缓存实现的详细介绍见{@link MethodArgumentResolverComposite}</p>
   */

  private final static Map<Class<?>, WebTypeConverter<?>> CACHED_CONVERTERS = new ConcurrentHashMap<>(32);

  static {
    CACHED_CONVERTERS.put(BigDecimal.class, new BigDecimalTypeConverter());
    CACHED_CONVERTERS.put(Boolean.class, new BooleanTypeConverter());
    CACHED_CONVERTERS.put(Boolean.TYPE, new BooleanTypeConverter());
    CACHED_CONVERTERS.put(Byte.class, new ByteTypeConverter());
    CACHED_CONVERTERS.put(Byte.TYPE, new ByteTypeConverter());
    CACHED_CONVERTERS.put(Character.class, new CharacterTypeConverter());
    CACHED_CONVERTERS.put(Character.TYPE, new CharacterTypeConverter());
    CACHED_CONVERTERS.put(Date.class, new DateTypeConverter());
    CACHED_CONVERTERS.put(Double.class, new DoubleTypeConverter());
    CACHED_CONVERTERS.put(Double.TYPE, new DoubleTypeConverter());
    CACHED_CONVERTERS.put(Float.class, new FloatTypeConverter());
    CACHED_CONVERTERS.put(Float.TYPE, new FloatTypeConverter());
    CACHED_CONVERTERS.put(Integer.class, new IntegerTypeConverter());
    CACHED_CONVERTERS.put(Integer.TYPE, new IntegerTypeConverter());
    CACHED_CONVERTERS.put(LocalDateTime.class, new LocalDateTimeTypeConverter());
    CACHED_CONVERTERS.put(LocalDate.class, new LocalDateTypeConverter());
    CACHED_CONVERTERS.put(LocalTime.class, new LocalTimeTypeConverter());
    CACHED_CONVERTERS.put(Long.class, new LongTypeConverter());
    CACHED_CONVERTERS.put(Long.TYPE, new LongTypeConverter());
    CACHED_CONVERTERS.put(Short.class, new ShortTypeConverter());
    CACHED_CONVERTERS.put(Short.TYPE, new ShortTypeConverter());
    CACHED_CONVERTERS.put(String.class, new StringTypeConverter());
  }

  /**
   * @param paramType 参数类型
   * @return 返回null表示没有此类型的类型转换器
   */
  public static WebTypeConverter<?> getTypeConverter(Class<?> paramType) {
    return CACHED_CONVERTERS.get(paramType);
  }

  /**
   * 此方法会把字符串数据转换为指定的简单类型，此方法对于不能转换的数据（没有转换器或转换出错）是采用抛异常的方式，而不是返回null的形式，
   * 因为要让异常往调用链上抛出以便用户知道数据有问题。
   * 此方法是可能返回null，比如原始数据本身就是null，经过StringTypeConverter转换后仍然是返回null的
   *
   * <p>此方法像一个门面模式方法,代表着所有{@link WebTypeConverter}的方法{@link WebTypeConverter#convert(String)},
   * 可以直接使用此方法,而不用写类似这样的代码{@code WebTypeConverters.getTypeConverter(parameter.getParameterType()).convert(value)}</p>
   *
   * @param paramType         简单类型
   * @param requestParamValue 原始web请求数据
   * @param <T>               要转换的目标类型
   * @return 返回转换成功后的数据
   * @throws Exception 但指定的类型没有对应的转换器会抛UnsupportedOperationException；有转换器但转换失败会抛IllegalArgumentException
   */
  @SuppressWarnings("RedundantThrows")
  public static <T> Object convert(Class<T> paramType, String requestParamValue) throws Exception {
    WebTypeConverter<?> typeConverter = getTypeConverter(paramType);
    if (typeConverter == null) {
      throw new UnsupportedOperationException("不支持对此类型:" + paramType.getName() + "的类型转换");
    }

    try {
      return typeConverter.convert(requestParamValue);
    } catch (Exception exception) {
      // 这里选择抛出异常，因为在简单类型解析器中，只有值不为null并且已经提取了默认值之后才会进行类型转换
      // 也就是说是有源数据进行类型转换的，如果出现异常，基本就是无法转换的情况，比如字符串转换为整数，所以这里往上抛异常,而不选择return null
      throw new IllegalArgumentException("无法将数据:[" + requestParamValue + "]转换为类型:" + paramType.getName());
    }
  }

}
