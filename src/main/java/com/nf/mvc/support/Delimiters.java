package com.nf.mvc.support;

import com.nf.mvc.util.StringUtils;

import javax.servlet.ServletConfig;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 此枚举类编写了常见分隔符的正则表达式，方便使用。
 * <p>枚举成员通常用作字符串拆分的分隔符使用，可以单独使用这些分隔符，
 * <pre class="code">
 *      List<String> stringList = StringUtils.split(data, Delimiters.Colon.getPattern());
 * </pre>
 * 也可以组合使用:
 * <pre class="code">
 *      String combinedPattern = Delimiters.getCombinedPattern(EnumSet.of( Delimiters.Colon,Delimiters.Common));
 *      List<String> stringList = StringUtils.split(data, combinedPattern);
 * </pre>
 * 组合使用是一种或者的关系，意味着字符串中只要出现了组合的模式中的任意成员都会进行拆分操作
 * </p>
 * <p>具体的使用地方见{@link com.nf.mvc.DispatcherServlet#getBasePackages(ServletConfig)}</p>
 *
 * @see StringUtils
 * @see com.nf.mvc.DispatcherServlet
 */
public enum Delimiters {
  /**
   * 逗号分隔符
   */
  Comma(",+", "逗号分隔符"),
  /**
   * 空格分隔符
   */
  Space("\\s+", "空格分隔符"),
  /**
   * 分号分隔符
   */
  SemiColon(";+", "分号分隔符"),
  /**
   * spring常用的分隔符，逗号，空格，分号
   */
  Common("[\\s,;]+", "spring常用的分隔符，逗号，空格，分号"),
  /**
   * 连字符分隔符
   */
  Hyphen("-+", "连字符分隔符"),
  /**
   * 冒号分隔符
   */
  Colon(":+", "冒号分隔符"),
  /**
   * 竖线分隔符(Vertical Line),也叫管道符
   */
  Pipe("\\|", "竖线分隔符");
  private final String pattern;
  private final String desc;

  Delimiters(String pattern, String desc) {
    this.pattern = pattern;
    this.desc = desc;
  }

  public String getPattern() {
    return pattern;
  }

  public String getDesc() {
    return desc;
  }

  /**
   * 此方法是用来处理定制的分隔符组合的pattern的
   * <h3>基本用法</h3>
   * <pre class="code">
   *    EnumSet<Delimiters> spaceAndColon = EnumSet.of(Delimiters.Space, Delimiters.Colon);
   *    String pattern = getCombinedPattern(spaceAndColon);
   * </pre>
   *
   * @param delimiters 所有分隔符枚举实例
   * @return 所有枚举实例对应的正则表达式，是或者的关系
   */
  public static String getCombinedPattern(EnumSet<Delimiters> delimiters) {
    List<String> patternList = delimiters.stream()
            .map(Delimiters::getPattern)
            .collect(Collectors.toList());
    return String.join("|", patternList);
  }
}
