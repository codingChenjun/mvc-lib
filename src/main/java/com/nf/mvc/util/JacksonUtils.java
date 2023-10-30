package com.nf.mvc.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.nf.mvc.support.converter.DateTypeConverter.DATETIME_PATTERN;
import static com.nf.mvc.support.converter.DateTypeConverter.DATE_PATTERN;
import static com.nf.mvc.support.converter.DateTypeConverter.TIME_PATTERN;

/**
 * 一个Jackson的序列化与反序列化工具类，对于序列化以及反序列化为普通类、泛型类、泛型集合类
 * 提供了简便的工具方法
 * <p>
 * 参考：
 * <a href="https://juejin.cn/post/6844904166809157639">jackson的入门教程</a>
 * <a href="https://www.baeldung.com/jackson-serialize-dates">日期的处理</a>
 * <a href="https://www.baeldung.com/jackson">jackson教程</a>
 * <a href="https://www.baeldung.com/java-deserialize-generic-type-with-jackson">泛型的反序列化</a>
 * </p>
 */
public abstract class JacksonUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    // 设置java.util.Date时间类的序列化以及反序列化的格式
    objectMapper.setDateFormat(new SimpleDateFormat(DATETIME_PATTERN));

    // 初始化JavaTimeModule
    JavaTimeModule javaTimeModule = new JavaTimeModule();

    // 处理LocalDateTime
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

    // 处理LocalDate
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
    javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

    // 处理LocalTime
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
    javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
    javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

    // 注册时间模块, 支持jsr310, 即新的时间类(java.time包下的各种时间类型)
    objectMapper.registerModule(javaTimeModule);
    // 序列化时默认包含所有字段
    objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    // 在序列化一个空对象时不抛出异常
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    // json文本的数据比pojo类多了一些内容，不报错，忽略这些多余的位置数据
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // 不把日期序列化为时间戳
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // 不对null值进行序列化
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * 典型的反序列化用法
   * <pre class="code">
   *     User u = JacksonUtils.fromJson(str,User.class);
   * </pre>
   *
   * @param json json文本
   * @param type 反序列化的类型
   * @param <T>  反序列化泛型
   * @return json文本反序列化之后的对象
   */
  public static <T> T fromJson(final String json, Class<T> type) {
    T data;
    try {
      data = getObjectMapper().readValue(json, type);
    } catch (Exception e) {
      throw new IllegalArgumentException("反序列化失败，是否数据格式不对?", e);
    }
    return data;
  }

  /**
   * 典型的反序列化用法2
   * <pre class="code">
   *     class Response<T>{
   *         private T result;
   *         //getter,setter
   *     }
   *     public class User {
   *         private Long id;
   *         private String firstName;
   *         private String lastName;
   *
   *       // getters and setters...
   *      }
   *      //反序列化为一个泛型类
   *     Response<User> res = JacksonUtils.fromJson(str,Response.class,User.class);
   *     //也可以反序列化为一个集合
   *     List<User> users = JacksonUtils.fromJson(str,List.class,User.class);
   * </pre>
   *
   * @param json         json文本
   * @param genericClass 普通的泛型类或者泛型集合类
   * @param actualType   普通泛型类的实参类型或者集合泛型类的实参类型
   * @param <T>          反序列化泛型
   * @return json文本反序列化之后的对象
   */
  public static <T> T fromJson(String json, Class<?> genericClass, Class<?>... actualType) {
    JavaType javaType = getObjectMapper().getTypeFactory()
            .constructParametricType(genericClass, actualType);
    try {
      return getObjectMapper().readValue(json, javaType);
    } catch (Exception e) {
      throw new IllegalArgumentException("反序列化失败，是否数据格式不对?", e);
    }
  }

  public static <T> T fromJson(final InputStream inputStream, Class<T> type) {
    T data;
    try {
      data = getObjectMapper().readValue(inputStream, type);
    } catch (Exception e) {
      throw new IllegalArgumentException("反序列化失败，是否数据格式不对?", e);
    }
    return data;
  }

  public static <T> T fromJson(final InputStream inputStream, Class<?> genericClass, Class<?>... actualType) {
    JavaType javaType = getObjectMapper().getTypeFactory()
            .constructParametricType(genericClass, actualType);
    try {
      return getObjectMapper().readValue(inputStream, javaType);
    } catch (Exception e) {
      throw new IllegalArgumentException("反序列化失败，是否数据格式不对?", e);
    }
  }

  public static String toJson(Object obj) {
    String json;
    try {
      json = getObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new IllegalArgumentException("序列化失败", e);
    }
    return json;
  }
}
