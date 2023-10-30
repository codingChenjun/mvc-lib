package com.nf.mvc.support;

import java.util.HashMap;
import java.util.Map;

public enum HttpMethod {
	/**
	 * get请求
	 */
	GET,
	/**
	 * head请求
	 */
	HEAD,
	/**
	 * post请求
	 */
	POST,
	/**
	 * put请求
	 */
	PUT,
	/**
	 * patch请求
	 */
	PATCH,
	/**
	 * delete请求
	 */
	DELETE,
	/**
	 * options请求
	 */
	OPTIONS,
	/**
	 * trace
	 */
	TRACE;

	private static final Map<String, HttpMethod> MAPPINGS = new HashMap<>(16);

	static {
		for (HttpMethod httpMethod : values()) {
			MAPPINGS.put(httpMethod.name(), httpMethod);
		}
	}

	public static HttpMethod resolve( String method) {
		return (method != null ? MAPPINGS.get(method) : null);
	}

	public boolean matches(String method) {
		return name().equals(method);
	}

}