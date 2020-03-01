package com.my.project.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.project.api.core.ApiResponse;
import com.my.project.constants.AuthorizedConstant;
import com.my.project.constants.RedisConstant;
import com.my.project.constants.UrlPathConstant;
import com.my.project.domain.entity.LoginUser;
import com.my.project.security.AuthenticationTokenImpl;
import com.my.project.security.AuthorizedService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
     * session保存时间
     * */
    private static final Long USER_SESSION_TIME = 60 * 60 * 4L;
    /**
     * 排序过滤的URL地址 login register swagger的api-docs
     * */
    private static final String[] whiteList = {"/user/user.login", "/user/register", "/system/v2/api-docs"};

    @Autowired
    private AuthorizedService authorizedService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String url = exchange.getRequest().getURI().getPath();
        if (OPTIONS.equalsIgnoreCase(request.getMethodValue()) || Arrays.asList(whiteList).contains(url)) {
            log.info("不需要进行Authorized认证的接口：[{}]", url);
            if (url.contains(UrlPathConstant.LOGIN_PTH)) {
                return filterLoginSession(exchange, chain);
            }
            return chain.filter(exchange);
        }

        if (!authorizedService.checkAuthorized(request)) {
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
            message = JSON.toJSONString(new ApiResponse(HttpStatus.UNAUTHORIZED.value(), msg)).getBytes("UTF-8");
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

    /**
     * 登录成功，回调函数，保存session
     * */
    private Mono<Void> filterLoginSession(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        ServerHttpResponseDecorator decoratorResponse = new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> dataBuffer) {
                if (dataBuffer instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) dataBuffer;
                    return super.writeWith(fluxBody.map(flux -> {

                        byte[] content = new byte[flux.readableByteCount()];
                        flux.read(content);
                        //释放掉内存
                        DataBufferUtils.release(flux);
                        String body = new String(content, Charset.forName("UTF-8"));
                        if (StringUtils.isNotBlank(body)) {
                            // 获取登录的结果是否是成功的
                            JSONObject bodyJson = JSON.parseObject(body);
                            log.info("login接口响应信息：[{}]", JSON.toJSONString(bodyJson));
                            if (null != bodyJson.get(UrlPathConstant.LOGIN_RESPONSE_STATUS)) {

                                Integer status = (Integer) bodyJson.get(UrlPathConstant.LOGIN_RESPONSE_STATUS);
                                if (HttpStatus.OK.value() == status) {
                                    // 生成token
                                    addAuthentication(exchange, bodyJson);
                                }
                            }
                        }
                        return bufferFactory.wrap(content);
                    }));
                }
                return super.writeWith(dataBuffer);
            }
        };
        return chain.filter(exchange.mutate().response(decoratorResponse).build());
    }


    private void addAuthentication(ServerWebExchange exchange, JSONObject bodyJson) {

        LoginUser user = JSONObject.parseObject(JSON.toJSONString(bodyJson), LoginUser.class);
        if (null != user) {
            String username = user.getUsername();
            AuthenticationTokenImpl auth = new AuthenticationTokenImpl(user, Collections.emptyList());
            auth.setDetails(user);
            ServerHttpResponse response = exchange.getResponse();
            // 存到redis中
            redisTemplate.opsForValue().set(RedisConstant.SESSION_REDIS_PREFIX + String.format("%s:%s", username.toLowerCase(), AuthorizedService.getHash(user)),
                                               user,
                                               USER_SESSION_TIME,
                                               TimeUnit.SECONDS);

            Map<String, Object> claims = new HashMap();
            claims.put(AuthorizedConstant.PRINCIPAL_NAME, username);
            claims.put("hash", AuthorizedService.getHash(user));
            String JWT = Jwts.builder().setSubject(username)
                                        .signWith(SignatureAlgorithm.HS512, AuthorizedConstant.SECRET)
                                        .setClaims(claims)
                                        .setExpiration(new Date(System.currentTimeMillis() + 1000 * USER_SESSION_TIME)).compact();
            response.getHeaders().set(AuthorizedConstant.AUTH_HEADER_NAME, JWT);
        }
    }
}
