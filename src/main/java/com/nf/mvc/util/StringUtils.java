package com.nf.mvc.util;

import com.nf.mvc.support.Assert;
import com.nf.mvc.support.Delimiters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 此类拷贝自spring的StringUtils类
 */
public abstract class StringUtils {
  private static final String[] EMPTY_STRING_ARRAY = {};
  private static final char BLANK = ' ';

  /**
   * 把List字符串数据变成一个分隔符分隔的字符串，比如List集合中有a b c三条记录，<br/>
   * 而指定的分隔符是逗号，那么调用此方法之后生成的字符串是：a,b,c
   *
   * @param stringList 字符串List集合
   * @param delimiter  分隔符
   * @return 合并字符串集合变成一个字符串，之间用分隔符分隔
   */
  public static String toCommaDelimitedString(Iterable<String> stringList, String delimiter) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for (String val : stringList) {
      if (val != null) {
        joiner.add(val);
      }
    }
    return joiner.toString();
  }

  /**
   * 把一个字符串转换成驼峰命名的字符串，在指定的分隔符处进行处理。
   * 比如text="get_by_id",delimiter="_",那么转换结果就是"getById"
   *
   * @param text      要进行转换的字符串
   * @param delimiter 分隔符，通常是下划线，空格之类的字符
   * @return 驼峰规范的字符串
   * @see <a href="https://www.baeldung.com/java-string-to-camel-case#:~:text=String%20camelCase%20%3D,CaseFormat.UPPER_UNDERSCORE.to%20%28CaseFormat.LOWER_CAMEL%2C%20%22THIS_STRING_SHOULD_BE_IN_CAMEL_CASE%22%29%3B">字符串转换为驼峰</a>
   */
  public static String toCamelCase(String text, char delimiter) {
    boolean shouldConvertNextCharToLower = true;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char currentChar = text.charAt(i);
      if (currentChar == delimiter) {
        shouldConvertNextCharToLower = false;
      } else if (shouldConvertNextCharToLower) {
        builder.append(Character.toLowerCase(currentChar));
      } else {
        builder.append(Character.toUpperCase(currentChar));
        shouldConvertNextCharToLower = true;
      }
    }
    return builder.toString();
  }

  public static boolean isEmpty(CharSequence str) {
    return (str == null || str.length() == 0);
  }

  /**
   * 判断字符串是否有数据，其中空白字符是不算有数据的，
   * 空白字符由方法{@link Character#isWhitespace(char)}决定
   *
   * @param str 字符串数据
   * @return 有内容就是true否则就是false
   */
  public static boolean hasText(String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * 此方法返回第一个非空白字符的索引位置，字符串中间有空格是不处理的，
   * 比如"   abc"这个字符串前面有3个空格，那么调用此方法后返回值是3，
   *
   * @param str 字符串数据
   * @return 返回第一个非空白字符的索引位置
   */
  public static int skipBlanks(String str) {
    int pointer = 0;
    while (!str.isEmpty() && pointer < str.length() && str.charAt(pointer) == BLANK) {
      pointer++;
    }
    return pointer;
  }

  /**
   * 字符串中任何位置有空白字符就返回true
   *
   * @param str 字符串数据
   * @return 有空白字符就返回true，否则返回false
   */
  public static boolean containsWhitespace(CharSequence str) {
    if (isEmpty(str)) {
      return false;
    }

    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsWhitespace(String str) {
    return containsWhitespace((CharSequence) str);
  }

  /**
   * 删除字符串前面的空白
   *
   * @param str the {@code String} to check
   * @return the trimmed {@code String}
   * @see java.lang.Character#isWhitespace
   */
  public static String trimLeadingWhitespace(String str) {
    if (isEmpty(str)) {
      return str;
    }

    int beginIdx = 0;
    while (beginIdx < str.length() && Character.isWhitespace(str.charAt(beginIdx))) {
      beginIdx++;
    }
    return str.substring(beginIdx);
  }

  /**
   * 删除字符串尾部的空白
   *
   * @param str the {@code String} to check
   * @return the trimmed {@code String}
   * @see java.lang.Character#isWhitespace
   */
  public static String trimTrailingWhitespace(String str) {
    if (isEmpty(str)) {
      return str;
    }

    int endIdx = str.length() - 1;
    while (endIdx >= 0 && Character.isWhitespace(str.charAt(endIdx))) {
      endIdx--;
    }
    return str.substring(0, endIdx + 1);
  }

  /**
   * 删除字符串前面所有指定的字符
   *
   * @param str              the {@code String} to check
   * @param leadingCharacter the leading character to be trimmed
   * @return the trimmed {@code String}
   */
  public static String trimLeadingCharacter(String str, char leadingCharacter) {
    if (isEmpty(str)) {
      return str;
    }

    int beginIdx = 0;
    while (beginIdx < str.length() && leadingCharacter == str.charAt(beginIdx)) {
      beginIdx++;
    }
    return str.substring(beginIdx);
  }

  /**
   * 删除字符串后面所有指定的字符
   *
   * @param str               the {@code String} to check
   * @param trailingCharacter the trailing character to be trimmed
   * @return the trimmed {@code String}
   */
  public static String trimTrailingCharacter(String str, char trailingCharacter) {
    if (isEmpty(str)) {
      return str;
    }

    int endIdx = str.length() - 1;
    while (endIdx >= 0 && trailingCharacter == str.charAt(endIdx)) {
      endIdx--;
    }
    return str.substring(0, endIdx + 1);
  }

  /**
   * 裁剪掉所有的空白字符，包含前部，中间，尾部的所有空白
   *
   * @param str 要处理的字符串
   * @return 剔除前后中间所有空白之后的字符串
   */
  public static String trimAllWhitespace(String str) {
    if (isEmpty(str)) {
      return str;
    }

    int len = str.length();
    StringBuilder sb = new StringBuilder(str.length());
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (!Character.isWhitespace(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * 利用String自带的split方法进行字符串拆分，拆分前会调用trim方法剔除掉前后所有的空白，
   * split的拆分方法可能导致前部出现空的字符串，比如用逗号作为分隔符拆分",,,abc,def"这样的数据，
   * 本方法剔除了拆分为长度为0的空字符串
   *
   * @param source:一个字符串数据
   * @param regex：正则表达式，通常是分隔符，可以使用{@link Delimiters}来指定常用的分隔符及分隔符组合
   * @return 拆分后的字符串集合
   */
  public static List<String> split(String source, String regex) {
    Assert.notNull(source, "原始字符串不能是null的");
    String[] strings = source.trim()
            .split(regex);
    return Arrays.stream(strings)
            .filter(s -> s.length() != 0)
            .collect(Collectors.toList());
  }

  public static String capitalize(String str) {
    return changeFirstCharacterCase(str, true);
  }

  /**
   * Uncapitalize a {@code String}, changing the first letter to
   * lower case as per {@link Character#toLowerCase(char)}.
   * No other letters are changed.
   *
   * @param str the {@code String} to uncapitalize
   * @return the uncapitalized {@code String}
   */
  public static String uncapitalize(String str) {
    return changeFirstCharacterCase(str, false);
  }

  private static String changeFirstCharacterCase(String str, boolean capitalize) {
    if (isEmpty(str)) {
      return str;
    }

    char baseChar = str.charAt(0);
    char updatedChar;
    if (capitalize) {
      updatedChar = Character.toUpperCase(baseChar);
    } else {
      updatedChar = Character.toLowerCase(baseChar);
    }
    if (baseChar == updatedChar) {
      return str;
    }

    char[] chars = str.toCharArray();
    chars[0] = updatedChar;
    return new String(chars);
  }


  public static boolean isNumeric(CharSequence str) {
    if (str == null || str.length() == 0) {
      return false;
    }
    final int size = str.length();
    for (int i = 0; i < size; i++) {
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String[] toStringArray(Collection<String> collection) {
    return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
  }

  public static String deleteAny(String inString, String charsToDelete) {
    if (isEmpty(inString) || isEmpty(charsToDelete)) {
      return inString;
    }

    StringBuilder sb = new StringBuilder(inString.length());
    for (int i = 0; i < inString.length(); i++) {
      char c = inString.charAt(i);
      if (charsToDelete.indexOf(c) == -1) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * 把一个字符串拆分成字符串数组,支持分隔符是多个字符,也支持拆分后删掉一些字符.
   * <p>假定你这样调用delimitedListToStringArray("abcxxdexxfg", "xx","acg")此方法,
   * 那么最终的结果就是三个字符串的数组:[b, de, f]
   * </p>
   * <p>此方法的代码来自于spring框架中的同名方法源代码</p>
   *
   * @param str           要处理的字符串
   * @param delimiter     分隔符字符串
   * @param charsToDelete 要删除掉的字符串
   * @return 拆分之后的字符串数组
   */
  public static String[] delimitedListToStringArray(
          String str, String delimiter, String charsToDelete) {

    if (str == null) {
      return EMPTY_STRING_ARRAY;
    }
    if (delimiter == null) {
      return new String[]{str};
    }

    List<String> result = new ArrayList<>();
    if (delimiter.isEmpty()) {
      for (int i = 0; i < str.length(); i++) {
        result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
      }
    } else {
      int pos = 0;
      int delPos;
      while ((delPos = str.indexOf(delimiter, pos)) != -1) {
        result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
        pos = delPos + delimiter.length();
      }
      if (str.length() > 0 && pos <= str.length()) {
        // Add rest of String, but not in case of empty input.
        result.add(deleteAny(str.substring(pos), charsToDelete));
      }
    }
    return toStringArray(result);
  }

  public static String[] addStringToArray(String[] array, String str) {
    if (ObjectUtils.isEmpty(array)) {
      return new String[]{str};
    }

    String[] newArr = new String[array.length + 1];
    System.arraycopy(array, 0, newArr, 0, array.length);
    newArr[array.length] = str;
    return newArr;
  }

  public static String[] tokenizeToStringArray(String str, String delimiters) {
    return tokenizeToStringArray(str, delimiters, true, true);
  }

  public static String[] tokenizeToStringArray(
          String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

    if (str == null) {
      return EMPTY_STRING_ARRAY;
    }

    StringTokenizer st = new StringTokenizer(str, delimiters);
    List<String> tokens = new ArrayList<>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (trimTokens) {
        token = token.trim();
      }
      if (!ignoreEmptyTokens || token.length() > 0) {
        tokens.add(token);
      }
    }
    return toStringArray(tokens);
  }
}
