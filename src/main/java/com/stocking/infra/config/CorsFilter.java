package com.stocking.infra.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsFilter implements Filter {

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMMON Area
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String X_REQUESTED_WITH = "X-Requested-With"; // Ajax Header
    public static final String ORIGIN = "Origin";
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // CORS Area
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String AUTHORIZATION = "authorization";

    private final StringBuilder allowHeaders = new StringBuilder().append(ACCEPT).append(", ").append(CONTENT_TYPE)
            .append(", ").append(X_REQUESTED_WITH) // Ajax Header
            .append(", ").append(ORIGIN).append(", ").append(ACCESS_CONTROL_REQUEST_HEADERS).append(", ")
            .append(ACCESS_CONTROL_REQUEST_METHOD).append(", ").append(ACCESS_CONTROL_ALLOW_ORIGIN).append(", ")
            .append(AUTHORIZATION);

    @Override
    public void destroy() {
        // ...
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            res.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            res.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE");
            res.setHeader(ACCESS_CONTROL_MAX_AGE, "3600");
            res.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, this.allowHeaders.toString());
            res.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");

            if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
                res.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            chain.doFilter(request, response);

        } catch (Exception e) {
            throw e;
        }
    }
}
