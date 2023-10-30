package com.nf.mvc.cors;

import com.nf.mvc.support.HttpMethod;
import com.nf.mvc.util.CollectionUtils;
import com.nf.mvc.util.ObjectUtils;
import com.nf.mvc.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CorsConfiguration {

    public static final String ALL = "*";

    private final List<String> allowedOrigins = new ArrayList<>();

    private final List<String> allowedMethods = new ArrayList<>();

    private final List<String> allowedHeaders = new ArrayList<>();

    private Boolean allowCredentials = false;

    private Long maxAge = 3600L;

    public CorsConfiguration allowedOrigins(String... origins) {
        CollectionUtils.mergeArrayIntoCollection(origins, allowedOrigins);
        return this;
    }

    public List<String> getAllowedOrigins() {
        return this.allowedOrigins;
    }

    public CorsConfiguration allowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public CorsConfiguration allowedHeaders(String... allowedHeaders) {
        CollectionUtils.mergeArrayIntoCollection(allowedHeaders, this.allowedHeaders);
        return this;
    }

    public List<String> getAllowedHeaders() {
        return this.allowedHeaders;
    }

    public CorsConfiguration allowedMethods(HttpMethod... allowedMethods) {
        for (HttpMethod allowedMethod : allowedMethods) {
            this.allowedMethods.add(allowedMethod.name());
        }
        return this;
    }

    public List<String> getAllowedMethods() {
        return this.allowedMethods;
    }

    public Long getMaxAge() {
        return this.maxAge;
    }

    public CorsConfiguration maxAge(Long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * 应用默认的跨域设置，设置如下
     * <ul>
     *     <li>origin设置为*</li>
     *     <li>允许的方法设置GET,POST,DELETE,PUT,OPTIONS</li>
     *     <li>不允许携带私密信息</li>
     *     <li>允许的头部设置为*</li>
     * </ul>
     */
    public void applyDefaultConfiguration(){
        allowedOrigins(ALL).
        allowCredentials(false).
        allowedMethods(HttpMethod.OPTIONS,HttpMethod.GET,HttpMethod.POST,HttpMethod.DELETE,HttpMethod.PUT).
        allowedHeaders(ALL);
    }

    public static CorsConfiguration defaultInstance(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyDefaultConfiguration();
        return corsConfiguration;
    }

    /**
     * 清理默认的跨域设置，origin是空，允许的方法也不设置，允许的头也没有任何设置
     */
    public void clearDefaultConfiguration(){
        this.allowedOrigins.clear();
        allowCredentials(false);
        this.allowedMethods.clear();
        this.allowedHeaders.clear();
    }

    public String checkOrigin( String origin) {
        if (!StringUtils.hasText(origin)) {
            return null;
        }
        String originToCheck = trimTrailingSlash(origin);
        if (!ObjectUtils.isEmpty(this.allowedOrigins)) {
            if (this.allowedOrigins.contains(ALL)) {
                validateAllowCredentials();
                return ALL;
            }
            for (String allowedOrigin : this.allowedOrigins) {
                if (originToCheck.equalsIgnoreCase(allowedOrigin)) {
                    return origin;
                }
            }
        }
        return null;
    }

    private String trimTrailingSlash(String origin) {
        return (origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin);
    }

    public void validateAllowCredentials() {
        if (this.allowCredentials.equals( Boolean.TRUE) && this.allowedOrigins.contains(ALL)) {

            throw new IllegalArgumentException(
                    "When allowCredentials is true, allowedOrigins cannot contain the special value \"*\" " +
                            "since that cannot be set on the \"Access-Control-Allow-Origin\" response header. " +
                            "To allow credentials to a set of origins, list them explicitly " +
                            "or consider using \"allowedOriginPatterns\" instead.");
        }
    }

}
