package com.nf.mvc.util;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * 这是一个主要对Class进行判断处理的工具类
 * <h3>集合类</h3>
 * 我的术语定义如下
 * <ul>
 *     <li>Collection就是jdk的语义，就是jdk中的Collection接口代表的类型，比如List，Set，Queue等</li>
 *     <li>Map保留jdk的语义，就是Map接口代表的类型</li>
 *     <li>Container，这是我定义的一个术语，代表jdk中的Collection+Map</li>
 *     <li>CommonContainer，这是我定义的一个术语，代表着List,Set,Map这三种类型</li>
 * </ul>
 */
public abstract class ClassUtils {
    /**
     * 这个map集合，key是包装类型（wrapper），值是此包装类型对应的基本类型(primitive)
     * IdentityHashMap表示的是key用==符号比较是true才相等，而不是普通HashMap用equals比较
     * ==符号表示指向的是同一个对象，equals表示的内容相等，简单来说，==是true就表示两者完全一样，
     * 那么equals也肯定是true，但equals是true不一定==也是true
     */
    static final Map<Class<?>, Class<?>> WRAPPER_TYPE_TO_PRIMITIVE_MAP = new IdentityHashMap<>(9);
    static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_MAP = new IdentityHashMap<>(9);

    static {
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Void.class, void.class);

        for (Map.Entry<Class<?>, Class<?>> entry : WRAPPER_TYPE_TO_PRIMITIVE_MAP.entrySet()) {
            PRIMITIVE_TYPE_TO_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 如果是基本类型、包装类型或者date，number等等就认为是简单类型
     *
     * @param type 类型信息
     * @return 基本类型、包装类型或者date，number类型就返回true，否则返回false
     */
    public static boolean isSimpleType(Class<?> type) {
        return (Void.class != type && void.class != type &&
                (isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Number.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type ||
                        LocalDate.class == type ||
                        LocalDateTime.class == type ||
                        LocalTime.class == type
                )
        );
    }

    /**
     * 判断是否是简单类型的数组，比如int[],Integer[],Date[]
     *
     * @param type 数据类型
     * @return 简单类型就返回true，否则返回false
     */
    public static boolean isSimpleTypeArray(Class<?> type) {
        return type.isArray() && isSimpleType(type.getComponentType());
    }

    public static boolean isCommonContainer(Class<?> type) {
        return isAssignableToAny(type, List.class, Set.class, Map.class);
    }

    public static boolean isContainer(Class<?> type) {
        return isAssignableToAny(type, Collection.class, Map.class);
    }

    public static boolean isList(Class<?> type) {
        return isAssignable(List.class, type);
    }

    public static boolean isSet(Class<?> type) {
        return isAssignable(Set.class, type);
    }

    public static boolean isMap(Class<?> type) {
        return isAssignable(Map.class, type);
    }

    /**
     * 判断是否是基本类型的包装类型，比如传递的参数是Integer就会返回true
     *
     * @param clazz：包装类型
     * @return 是包装类型就返回true，否则返回false
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return WRAPPER_TYPE_TO_PRIMITIVE_MAP.containsKey(clazz);
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    /**
     * 判断一个类是否是简单类型或者简单类型对应的包装类型
     *
     * @param clazz 类型信息
     * @return 是基本类型或包装类型就返回true，否则返回false
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * 判断第二个参数是否是第一个参数的子类或者实现类
     * 也考虑了基本类型，比如Integer与int认为是isAssignable
     * assignable：可赋值的
     *
     * @param lhsType 左手边类型信息
     * @param rhsType 右手边类型信息
     * @return 右手边类型可以赋值给左手边类型时返回true，否则返回false
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = WRAPPER_TYPE_TO_PRIMITIVE_MAP.get(rhsType);
            return (lhsType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(rhsType);
            return (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper));
        }
    }

    public static boolean isAssignableToAny(Class<?> lhsType, Class<?>... rhsTypes) {
        boolean isAssignable = false;
        for (Class<?> rhsType : rhsTypes) {
            isAssignable = isAssignable(rhsType, lhsType);
            if (isAssignable) {
                break;
            }
        }
        return isAssignable;
    }
}
