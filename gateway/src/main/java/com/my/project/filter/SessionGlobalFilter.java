package com.my.project.filter;

import com.alibaba.fastjson.JSON;
import com.my.project.api.core.ApiResponse;
import com.my.project.domain.entity.LoginUser;
import com.my.project.security.AuthorizedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/19 下午9:24
 */
@Component
@Slf4j
public class SessionGlobalFilter implements GlobalFilter, Ordered {

    /**
     * OPTIONS: 请求预处理，发送ajax请求时，有时network中会出现两次，前面一次就是OPTIONS，后面一次才是真正的请求
     * */
    private static final String OPTIONS = "OPTIONS";
    /**
     * 排序过滤的URL地址 login register swagger的api-docs
     * */
    private static final String[] whiteList = {"/user/login", "/user/register", "/system/v2/api-docs"};

    @Autowired
    private AuthorizedService authorizedService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String url = exchange.getRequest().getURI().getPath();
        if (OPTIONS.equalsIgnoreCase(request.getMethodValue()) || Arrays.asList(whiteList).contains(url)) {
            log.info("不需要进行Authorized认证的接口：[{}]", url);
            return chain.filter(exchange);
        }

        if (authorizedService.checkAuthorized(request)) {
            return setUnauthorizedResponse(exchange, "Authorized can't null");
        }

        LoginUser user = getPrincipal(request);
        // user == null 说明效验失败
        if (null == user) {
            return setUnauthorizedResponse(exchange, "Authorized verify error");
        }
        return chain.filter(exchange);
    }

    private Mono<Void> setUnauthorizedResponse(ServerWebExchange exchange, String msg) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        byte[] message = null;
        try {
            message = JSON.toJSONString(new ApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), msg)).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        DataBuffer buffer = response.bufferFactory().wrap(message);
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public LoginUser getPrincipal(ServerHttpRequest request) {

        Authentication authentication = authorizedService.getAuthentication(request);
        if (null != authentication) {
            return (LoginUser) authentication.getDetails();
        }
        return null;
    }
}
