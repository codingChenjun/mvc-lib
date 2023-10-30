package com.nf.mvc.argument;

/**
 * 此类是用来设定一些常量值的，主要用在RequestParam注解里设置属性的默认值使用
 * @see RequestParam
 */
public interface ValueConstants {
    /**
     * 这个值没有意义，就是搞一个基本不可能被用户指定的值，以便确定用户的确是指定了值,见{@link RequestParam},
     * 也通过设定这个常量值，也尽量的避免了不设置值时产生的一些空引用异常或者默认值不适合设置为null,""等特殊情况
     * */
    String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";
}
