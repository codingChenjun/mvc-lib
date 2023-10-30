package com.nf.mvc.support.vo;

public enum CommonResultCode implements ResultCode {
    /**
     * 表示调用成功，对应Http 状态码200
     */
    SUCCESS(200,"调用成功"),
    /**
     * 表示调用失败，对应Http 状态码500
     */
    FAILED(500,"调用失败"),
    /**
     * 表示没有权限，对应Http 状态码403
     */
    FORBIDDEN(403,"没有权限访问资源");

    private final Integer code;
    private final String message;

    CommonResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
