package com.nf.mvc.support.vo;


/**
 * 统一的响应结构类型，优先推荐使用静态方法，当静态方法的各个重载不满足要求时，可以直接使用构造函数来实例化此类的实例
 * @see ResultCode
 * @see CommonResultCode
 * @param <T>:响应数据
 */
public class ResponseVO<T> {
    private Integer code;
    private String message;
    private T data;

    public ResponseVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>(CommonResultCode.SUCCESS.getCode(), CommonResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResponseVO<T> success(String message,T data) {
        return new ResponseVO<>(CommonResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResponseVO<T> success(Integer code,T data) {
        return new ResponseVO<>(code,  CommonResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResponseVO<T> success(ResultCode resultCode,T data) {
        return new ResponseVO<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    public static <T> ResponseVO<T> fail() {
        return new ResponseVO<>(CommonResultCode.FAILED.getCode(), CommonResultCode.FAILED.getMessage(), null);
    }

    public static <T> ResponseVO<T> fail(String message) {
        return new ResponseVO<>(CommonResultCode.FAILED.getCode(), message, null);
    }

    public static <T> ResponseVO<T> fail(Integer code,String message) {
        return new ResponseVO<>(code, message, null);
    }

    public static <T> ResponseVO<T> fail(ResultCode resultCode) {
        return new ResponseVO<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseVO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
