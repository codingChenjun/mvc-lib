package com.nf.mvc.util;

import com.nf.mvc.support.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import static com.nf.mvc.support.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static com.nf.mvc.support.HttpHeaders.ORIGIN;

/**
 * 此类参考了spring的CorsUtils类
 */
public abstract class CorsUtils {

	/**
	 * 判断是否是跨域请求
	 */
	public static boolean isCorsRequest(HttpServletRequest request) {
		/*更好的实现见spring mvc中CorsUtils类，这里简化为只要有origin就表示是跨域请求*/
		return request.getHeader(ORIGIN) != null;
	}

	/**
	 * 判断是否是<i>预检请求</i>
	 */
	public static boolean isPreFlightRequest(HttpServletRequest request) {
		return (HttpMethod.OPTIONS.matches(request.getMethod()) &&
				request.getHeader(ORIGIN) != null &&
				request.getHeader(ACCESS_CONTROL_REQUEST_METHOD) != null);
	}

}