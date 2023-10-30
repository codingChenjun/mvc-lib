package com.nf.mvc.support.path;

import com.nf.mvc.support.PathMatcher;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 此类功能与{@link AntPathMatcher}类似，只提供了*与?这两个常见的通配符支持，
 * 不支持**这种通配符，并且比较默认是不区分大小写的。
 * 此类不提供任何的设置选项，比如matchStart,trimTokens等，
 * 设计此类时也不考虑对此类功能扩展方面的内容，所以加了一个final修饰符
 *
 * <h3>典型示例</h3>
 * 比如这样的路径模式"/a/&#042;/c/&#042;.js?",路径/a/bb/c/abc.jsp是匹配的
 * <h3>设计说明</h3>
 * <p>此类的命名没有特殊的意义，看到Ant（蚂蚁）就想到了Spider（蜘蛛）这个词汇，
 * 此类主要是用来集中存放正则表达式相关的资料</p>
 *
 * <h3>参考资料</h3>
 * <ul>
 * <li>参考spring的PathMatcher，AntPathMatcher。现在spring 5.0有另一个新的路径处理的类PathPattern（spring 5.0才出现）</li>
 * <li><a href="https://github.com/azagniotov/ant-style-path-matcher">AntPathMatcher简单实现</a></li>
 * <li><a href="https://my.oschina.net/iqoFil/blog/221623">spring AntPathMatcher匹配算法解析</a></li>
 * <li><a href="https://www.nationalfinder.com/html/char-asc.htm">html ascii码字母,文档注释中*的转译</a></li>
 * <li><a href="https://wangwl.net/static/projects/visualRegex#">正则表达式可视化与语义解释</a></li>
 * </ul>
 * @see com.nf.mvc.support.PathMatcher
 * @see EqualIgnoreCasePathMatcher
 * @see EqualPathMatcher
 * @see AntPathMatcher
 * @see com.nf.mvc.mapping.RequestMappingHandlerMapping
 * @see com.nf.mvc.argument.PathVariableMethodArgumentResolver
 * @author cj
 */
public final class  SpiderPathMatcher implements PathMatcher {
    /**
     * 把下面的正则表达式放到类上面的参考网址，可以知道其含义如下
     * 匹配问号（?）、星号（*）或者花括号（{}）中的任意字符（除了斜杠/）。
     *
     * 具体解释如下：
     * - \?：匹配问号字符（?）。
     * - \|：表示或的意思，用于连接多个匹配项。
     * - \*：匹配星号字符（*）。
     * - \{[^/]+?}：匹配花括号（{}）中的任意字符（除了斜杠/），并且使用非贪婪模式（+?）进行匹配。
     *
     * 总结起来，这个正则表达式可以用来匹配问号、星号或者花括号中的任意字符（除了斜杠/）
     */
    private static final Pattern PATH_PATTERN =Pattern.compile("\\?|\\*|\\{[^/]+?}");
    private static final String PATH_VARIABLE_PATTERN = "[^/]+?";
    @Override
    public boolean isMatch(String pattern, String path) {
        if (path.isEmpty() || pattern.isEmpty()) {
            return false;
        }
        Pattern pathPattern = buildPathPattern(pattern);
        return pathPattern.matcher(path).matches();
    }

    /**
     * 用来获取不同路径模式的比较器，基本比较规则就是路径模式中有通配符就靠后，
     * 目前的实现没有用到参数 path
     * @param path 要与路径模式进行匹配的路径
     * @return 比较器
     */
    @Override
    public Comparator<String> getPatternComparator(String path) {
        return (p1,p2)->p1.equals(p2)?0
                :haveWildcard(p1)?1:-1;
    }

    /**
     * 这个方法是依据路径模式来生成其对应的正则表达式，路径模式支持“*”与“?"
     * “*”代表任何数量的字符，？代表一个字符，类似于windows操作系统的通配符概念
     * 比如你写的路径模式是/a/&#042;/&#042;.js?,那么转换的思路
     * <ul>
     *     <li>*:转成.*,“句号”代表任意字符，“*”代表任意数量</li>
     *     <li>?:转成.,代表任意一个字符</li>
     *     <li>{a}:转成([^/])+?,任意字符（除了斜杠/），并且使用非贪婪模式（+?）</li>
     * </ul>
     * @param pattern 路径模式
     * @return 路径模式的正则表达式
     */
    private  Pattern buildPathPattern(String pattern) {
        StringBuilder patternBuilder = new StringBuilder();
        Matcher matcher = PATH_PATTERN.matcher(pattern);
        int end = 0;
        while (matcher.find()) {
            patternBuilder.append(quote(pattern, end, matcher.start()));
            String match = matcher.group();
            if ("?".equals(match)) {
                patternBuilder.append('.');
            }
            else if ("*".equals(match)) {
                patternBuilder.append(".*");
            }
            else if (isPathVariable(match)) {
                patternBuilder.append(PATH_VARIABLE_PATTERN);
            }
            end = matcher.end();
        }
        return Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    private  String quote(String s, int start, int end) {
        if (start == end) {
            return "";
        }
        return Pattern.quote(s.substring(start, end));
    }

    private boolean haveWildcard(String path) {
        return path.contains("?") || path.contains("*");
    }
}
