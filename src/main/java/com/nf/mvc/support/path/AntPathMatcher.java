package com.nf.mvc.support.path;


import com.nf.mvc.support.PathMatcher;
import com.nf.mvc.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nf.mvc.util.StringUtils.skipBlanks;

/**
 * 此类参考spring 的AntPathMatcher实现.
 * <p>ant地址主要用在web的url路径匹配上,其中三个核心通配符含义如下</p>
 * <ul>
 *   <li>'?' - 匹配一个字符</li>
 *   <li>'*' - 匹配0个或多个字符</li>
 *   <li>'**' - 在一个路径中匹配0个或多个部分(directory)</li>
 * </ul>
 * <h3>常见的匹配情况(spring的AntPathMatcher实现)</h3>
 * <ul>
 *   <li>"/".match("")==false;"/".matchStart("")==false</li>
 *   <li>"/a".match("/a/")==false;"/a".matchStart("/a/")==false</li>
 *   <li>"/a/".match("/a")==false;"/a/".matchStart("/a")==false</li>
 *   <li>"/*".match("")==false;"/*".matchStart("")==false</li>
 *   <li>"/a/*".match("/a")==false;"/a/*".matchStart("/a")==true</li>
 *   <li>"/a/*".match("/a/")==true;"/a/*".matchStart("/a/")==true</li>
 *   <li>"/a/**".match("/a")==true;"/*".matchStart("/a")==true</li>
 * </ul>
 * <h3>核心特性</h3>
 * 在此类的实现中,有4个选项是可以调整的,可以利用{@link AntPathMatcher.Builder}的对应方法进行调整
 * <ul>
 *   <li>pathSeparator:路径分隔符,默认值是"/"</li>
 *   <li>ignoreCase:忽略大小写,默认值是false</li>
 *   <li>trimTokens:删除前后空白,默认是false</li>
 *   <li>matchStart:模式前面部分匹配路径就算匹配,默认值是false</li>
 * </ul>
 * <h3>基本使用</h3>
 * <ul>
 *   <li>调用{@link AntPathMatcher#isMatch(String, String)}判断字符串是否符合模式</li>
 *   <li>调用{@link AntPathMatcher#extractPathVariables(String, String)}提取路径变量数据</li>
 * </ul>
 * <h3>matchStart</h3>
 * <p>关于matchStart的含义,下面一个示例代码解释了其具体含义,其中pathMatcher2设置matchStart为true,
 * 可以看到当设置matchStart位true之后,整个路径只要与pattern对应的前面部分匹配,模式即使多了一些内容,
 * 比如这里的d这一部分,也算是匹配</p>
 * <pre class="code">
 *         AntPathMatcher pathMatcher = new AntPathMatcher.Builder().build();
 *         String pattern = "a/&#042;/c/d";
 *         String path = "a/b0/c";
 *         boolean match = pathMatcher.isMatch(pattern, path);//false
 *         System.out.println("match = " + match);
 *         AntPathMatcher pathMatcher2 = new AntPathMatcher.Builder().withMatchStart().build();
 *         boolean matchStart = pathMatcher2.isMatch(pattern, path); //true
 *         System.out.println("matchStart = " + matchStart);
 * </pre>
 * @see com.nf.mvc.support.PathMatcher
 * @see EqualIgnoreCasePathMatcher
 * @see EqualPathMatcher
 * @see com.nf.mvc.mapping.RequestMappingHandlerMapping
 * @see com.nf.mvc.argument.PathVariableMethodArgumentResolver
 * @author cj
 */
public class AntPathMatcher implements PathMatcher {
  private static final char ASTERISK = '*';
  private static final char SLASH = '/';
  private static final char QUESTION = '?';
  /**
   * 路径变量写法的正则表达式,比如{pageSize}这种写法就匹配这个正则表达式,
   * spring源码中的正则表达式是:"\\{[^/]+?\\}",网上找到了一个表达式是
   * \\{.*?\\},IDEA提示有冗余,删除之后就是下面的表达式,关于正则表达式语义
   * 可以见类上面的参考网址.两个表达式的重大区别是:spring的模式认为大括号
   * 之间没有字符不算路径变量,比如{}这种情况spring就不认为是路径变量
   */
  private static final String PATH_VARIABLE_PATTERN = "\\{.*?}";
  private static final int ASCII_CASE_DIFFERENCE_VALUE = 32;
  private static final int LENGTH_OF_SLASH_ASTERISK = 2;

  private final char pathSeparator;
  private final boolean ignoreCase;
  private final boolean matchStart;
  private final boolean trimTokens;

  private AntPathMatcher(final char pathSeparator, boolean ignoreCase, boolean matchStart, boolean trimTokens) {
    this.pathSeparator = pathSeparator;
    this.ignoreCase = ignoreCase;
    this.matchStart = matchStart;
    this.trimTokens = trimTokens;
  }

  @Override
  public boolean isMatch(String pattern, String path) {
    //这个if块就是用来把路径变量(PathVariable)替换为一个*来处理匹配问题的
    if (StringUtils.hasText(pattern)) {
      pattern = pattern.replaceAll(PATH_VARIABLE_PATTERN, "*");
    }
    if (pattern.isEmpty()) {
      return path.isEmpty();
    } else if (path.isEmpty() && pattern.charAt(0) == pathSeparator) {
      if (matchStart) {
        return true;
      } else if (pattern.length() == LENGTH_OF_SLASH_ASTERISK && pattern.charAt(1) == ASTERISK) {
        return false;
      }
      return isMatch(pattern.substring(1), path);
    }

    final char patternStart = pattern.charAt(0);
    if (patternStart == ASTERISK) {
      if (pattern.length() == 1) {
        return path.isEmpty() || path.charAt(0) != pathSeparator && isMatch(pattern, path.substring(1));
      } else if (doubleAsteriskMatch(pattern, path)) {
        return true;
      }

      int start = 0;
      while (start < path.length()) {
        if (isMatch(pattern.substring(1), path.substring(start))) {
          return true;
        }
        start++;
      }
      return isMatch(pattern.substring(1), path.substring(start));
    }

    int pointer = trimTokens ? skipBlanks(path) : 0;

    return !path.isEmpty() && (charEqual(path.charAt(pointer), patternStart) || patternStart == QUESTION)
            && isMatch(pattern.substring(1), path.substring(pointer + 1));
  }

  /**
   * 获取路径变量及其值的方法,这是个简化的实现,没有使用spring的实现.
   * <p>比如路径模式是/list/{pageNo}/{pageSize},路径是/list/2/5,
   * 那么返回的map大致是这样的:[{pageNo:2},{pageSize:5}]</p>
   *
   * @param pattern 路径模式
   * @param path    路径
   * @return 包含各个路径变量及其值的Map
   */
  @Override
  public Map<String, String> extractPathVariables(String pattern, String path) {
    validateDoubleAsterisk(pattern);

    Map<String, String> pathVariables = new LinkedHashMap<>();
    if (!isMatch(pattern, path)) {
      return pathVariables;
    }

    String[] patternSegments = StringUtils.tokenizeToStringArray(pattern, String.valueOf(this.pathSeparator), this.trimTokens, true);
    String[] pathSegments = StringUtils.tokenizeToStringArray(path, String.valueOf(this.pathSeparator), this.trimTokens, true);
    for (int i = 0; i < pathSegments.length; i++) {
      String patternSegment = patternSegments[i];
      // 大于2是确保{}之间有字符
      if (patternSegment.length() > 2 && patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
        String variableName = patternSegment.substring(1, patternSegment.length() - 1);
        String variableValue = pathSegments[i];
        pathVariables.put(variableName, variableValue);
      }
    }
    return pathVariables;
  }

  private void validateDoubleAsterisk(String pattern) {
    int posDoubleAsterisk = pattern.indexOf("/**");
    int posPathVar = -1;
    Pattern reg = Pattern.compile(PATH_VARIABLE_PATTERN);
    Matcher matcher = reg.matcher(pattern);
    if (matcher.find()) {
      // 获取第一个匹配的路径变量模式的位置
      posPathVar = matcher.start();
    }

    if (posDoubleAsterisk >-1 && posPathVar >-1
            && posDoubleAsterisk < posPathVar) {
      throw new UnsupportedOperationException("不支持/**通配符在路径变量的前面");
    }
  }

  private boolean doubleAsteriskMatch(String pattern, String path) {
    if (pattern.charAt(1) != ASTERISK) {
      return false;
    } else if (pattern.length() > LENGTH_OF_SLASH_ASTERISK) {
      return isMatch(pattern.substring(3), path);
    }
    return false;
  }

  @Override
  public Comparator<String> getPatternComparator(String path) {
    return new AntPatternComparator(path);
  }

  private boolean charEqual(char pathChar, char patternChar) {
    if (ignoreCase) {
      return pathChar == patternChar ||
              ((pathChar > patternChar) ?
                      pathChar == patternChar + ASCII_CASE_DIFFERENCE_VALUE :
                      pathChar == patternChar - ASCII_CASE_DIFFERENCE_VALUE);
    }
    return pathChar == patternChar;
  }

  public static final class Builder {

    private char pathSeparator = SLASH;
    private boolean ignoreCase = false;
    private boolean matchStart = false;
    private boolean trimTokens = false;

    public Builder() {

    }

    public Builder withPathSeparator(final char pathSeparator) {
      this.pathSeparator = pathSeparator;
      return this;
    }

    public Builder withIgnoreCase() {
      this.ignoreCase = true;
      return this;
    }

    public Builder withMatchStart() {
      this.matchStart = true;
      return this;
    }

    public Builder withTrimTokens() {
      this.trimTokens = true;
      return this;
    }

    public AntPathMatcher build() {
      return new AntPathMatcher(pathSeparator, ignoreCase, matchStart, trimTokens);
    }
  }
}