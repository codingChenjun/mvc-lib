package com.nf.mvc.handler;

import com.nf.mvc.util.ReflectionUtils;


/**
 * 此类是一个封装handler相关信息的类型,现在支持以下几种类型的Handler
 * <ul>
 *     <li>只有handlerClass信息，无方法信息，handler的实例化与具体调用哪个方法由具体的{@link com.nf.mvc.HandlerAdapter}决定</li>
 *     <li>只有Handler信息，无方法信息，handler的实例化不归mvc框架处理，具体调用哪个方法由具体的{@link com.nf.mvc.HandlerAdapter}决定</li>
 *     <li>有HandlerClass信息，有方法信息，handler的实例化由mvc框架处理</li>
 *     <li>有Handler信息，有方法信息，handler实例化就不需要mvc框架处理</li>
 * </ul>
 *
 * <p>
 *     此类型的典型用法是：
 *     <ol>
 *         <li>某个HandlerMapping依据一定的规则解析所有的Handler，
 *         调用此类的2个构造函数中的一个实例化HandlerClass对象，并保存到HandlerMapping的容器中
 *              <ul>
 *                  <li>调用{@link #HandlerClass(Class)}此构造函数，意味HandlerMapping自己负责类的加载，Handler的实例化由mvc框架负责</li>
 *                  <li>调用{@link #HandlerClass(Object)}此构造函数，意味着Handler实例不由mvc框架负责，mvc直接用此实例调用其内部的某个方法处理请求</li>
 *              </ul>
 *         </li>
 *         <li>某个HandlerAdapter调用{@link #getHandlerObject()}获取Handler对象实例，
 *         HandlerAdapter决定调用的方法之后，就利用此Handler实例进行方法调用</li>
 *     </ol>
 * </p>
 * <p>
 *     到底什么是Handler？准确的定义就是处理请求的东西，也就是一段代码块，
 *     由于java编程语言的限制，代码块只能放在方法中，而方法又只能放在类中，
 *     这点与c#不一样，c#中，方法是第一等公民，是可以独立于类存在的。
 *     所以，如果用c#实现mvc框架，那么handler就可以认为是一个方法，但java就不能这么说，
 *     因为java中的方法不能脱离于类。所以导致Handler到底是类还是一个方法的理解困难。<br/>
 *
 *    而我这个mvc框架，为了更灵活，handler可以是一个类，也可以是一个方法，当是一个类时用HandlerClass代表，
 *    那到底由哪个方法处理请求呢？HandlerMapping是不管的，由HandlerAdapter去决定，
 *    可以见{@link com.nf.mvc.mapping.NameConventionHandlerMapping},  如果Handler是一个方法，
 *    那么就由HandlerMethod代表，由于方法不能脱离于类，所以HandlerMethod继承于HandlerClass类型，
 * </p>
 * @see com.nf.mvc.HandlerAdapter
 * @see com.nf.mvc.adapter.RequestMappingHandlerAdapter
 */
public class HandlerClass {
    private Class<?> handlerClass;
    private Object handlerObject;

    public HandlerClass(Class<?> handleClass) {
        this.handlerClass = handleClass;
    }

    public HandlerClass(Object handleObject) {
        this.handlerObject = handleObject;
    }

    public String getSimpleName(){
        return handlerClass!=null?
                handlerClass.getSimpleName():
                handlerObject.getClass().getSimpleName();
    }
    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Object getHandlerObject() {
        if (handlerObject != null) {
            return  handlerObject;
        }
        return ReflectionUtils.newInstance(handlerClass);
    }
}
