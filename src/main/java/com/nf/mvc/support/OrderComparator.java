package com.nf.mvc.support;

import java.util.Comparator;

/**
 * 此比较器是依据类上设定的注解{@link Order}值来进行比较的，数字越大优先级越低，
 * 其广泛应用在用户自定义的Mvc扩展组件之上，用来调整这些自定义组件的顺序
 * @param <T>
 */
public class OrderComparator<T> implements Comparator<T> {

    /**
     * 这里的实现要用减号，不要用类似o1Order>o2Order?1:-1这种写法，因为不满足自反性，
     * 见阿里巴巴java开发手册的说明
     * @param o1 第一个修饰了Order注解的对象
     * @param o2 第二个修饰了Order注解的对象
     * @return 比较结果
     */
    @Override
    public int compare(T o1, T o2) {
        int o1Order = getOrderValue(o1);
        int o2Order = getOrderValue(o2);
        return  o1Order - o2Order;
    }

    private int getOrderValue(T o){
        return o.getClass().isAnnotationPresent(Order.class)?
                o.getClass().getDeclaredAnnotation(Order.class).value():
                Integer.MAX_VALUE;

    }
}
