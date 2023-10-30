package com.nf.mvc.support;

import com.nf.mvc.support.path.AntPathMatcher;
import com.nf.mvc.support.path.EqualIgnoreCasePathMatcher;
import com.nf.mvc.support.path.EqualPathMatcher;
import com.nf.mvc.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 路径模式匹配的接口，主要应用在HandlerMapping组件与拦截器组件里，
 * 用来解析当前请求用哪一个Handler去处理(见{@link com.nf.mvc.mapping.RequestMappingHandlerMapping#getHandler(HttpServletRequest)})
 * 以及拦截器是否要应用到当前请求上(见:{@link com.nf.mvc.mapping.RequestMappingHandlerMapping#getInterceptors(HttpServletRequest)})
 *
 * @see AntPathMatcher
 * @see EqualPathMatcher
 * @see EqualIgnoreCasePathMatcher
 * @see com.nf.mvc.mapping.RequestMappingHandlerMapping
 */
public interface PathMatcher {
  PathMatcher DEFAULT_PATH_MATCHER = new AntPathMatcher.Builder().build();

  /**
   * 目前的实现中支持把**写在路径中间,比如/a/&#042;&#042;/c 这种写法,
   * 但spring最新版不建议这样写,会引起歧义,类似可变长度的方法参数一样,最好用在路径的最后一段里
   *
   * @param pattern:模式，比如/*,
   * @param path:具体的请求地址/a
   * @return 匹配就返回true，否则返回false
   */
  boolean isMatch(final String pattern, final String path);

  default Comparator<String> getPatternComparator(String path) {
    throw new UnsupportedOperationException("不支持对模式进行比较");
  }

  /**
   * 这个路径变量的默认实现是基于pattern与path的分段是相等的思路来实现的，
   * 实现类有不同实现逻辑可以重写此方法的实现
   * @param pattern 路径模式
   * @param path 路径
   * @return 路径变量Map集合，键是路径变量名，值是对应的值
   */
  default Map<String, String> extractPathVariables(String pattern, String path) {
    if (!isMatch(pattern, path)) {
      throw new IllegalArgumentException("路径:" + path + "不匹配路径模式:" + pattern + " ,提取路径变量没有意义");
    }
    Map<String, String> pathVariables = new HashMap<>();

    String[] patternDirs = StringUtils.tokenizeToStringArray(pattern, "/");
    String[] pathDirs = StringUtils.tokenizeToStringArray(path, "/");
    for (int i = 0; i < patternDirs.length; i++) {
      String patternDir = patternDirs[i];
      String pathDir = pathDirs[i];
      if (isPathVariable(patternDir)) {
        String varName = patternDir.substring(1, patternDir.length() - 1);
        pathVariables.put(varName, pathDir);
      }
    }
    return pathVariables;
  }

  default boolean isPathVariable(String varPattern) {
    return varPattern.startsWith("{") && varPattern.endsWith("}") && varPattern.length() > 2;
  }
}
