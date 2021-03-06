package com.api.core;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * @author coderyong
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    public static boolean printHeaders = true;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头信息
        Map<String, Object> header = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(key);
            while (headers.hasMoreElements()) {
                header.put(key, headers.nextElement());
            }
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("FromClient", getClientIp(request));
        requestMap.put("RequestUrl", "http://localhost:" + request.getServerPort() + request.getRequestURI());
        if (printHeaders) {
            //添加请求头信息
            requestMap.put("Header", header);
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            //添加请求参数信息
            requestMap.put("Parameters", parameterMap);
        }
        //打印请求信息
        Logger.json(JSON.toJSONString(requestMap));
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("x-forwarded-for");
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
            if (clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1")) {
                try {
                    //根据网卡取本机配置的IP
                    InetAddress address = InetAddress.getLocalHost();
                    clientIp = address.getHostAddress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (clientIp != null && clientIp.length() > 15) {
            if (clientIp.indexOf(",") > 0) {
                clientIp = clientIp.substring(0, clientIp.indexOf(","));
            }
        }
        return clientIp;
    }
}
