package com.my.project.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.project.constants.AuthorizedConstant;
import com.my.project.constants.RedisConstant;
import com.my.project.domain.entity.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.DigestUtils;

import java.util.Collections;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/23 下午8:01
 */
@Slf4j
public class AuthorizedService {

    private static final Long USER_EXPIRED_TIME = 5 * 60 * 60 * 1000L;

    private RedisTemplate redisTemplate;

    public AuthorizedService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean checkAuthorized(ServerHttpRequest request) {

        String token = request.getHeaders().getFirst(AuthorizedConstant.AUTH_HEADER_NAME);
        if (StringUtils.isNotBlank(token)) {
            return true;
        }
        return false;
    }

    public Authentication getAuthentication(ServerHttpRequest request) {

        String token = request.getHeaders().getFirst(AuthorizedConstant.AUTH_HEADER_NAME);
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        Claims claims = parseJWT(token);
        if (claims.containsKey(AuthorizedConstant.PRINCIPAL_NAME)) {

            String username = claims.get(AuthorizedConstant.PRINCIPAL_NAME).toString();
            String hash = claims.get(AuthorizedConstant.HASH_CLAIMS).toString();
            Object value = redisTemplate.opsForValue().get(RedisConstant.SESSION_REDIS_PREFIX + String.format("%s:%s", username.toLowerCase(), hash));

            if (null == value) {
                return null;
            }
            LoginUser user = JSONObject.parseObject(JSON.toJSONString(value), LoginUser.class);
            AuthenticationTokenImpl auth = new AuthenticationTokenImpl(user, Collections.<GrantedAuthority>emptyList());
            auth.setDetails(user);
            // 判断该用户token是否已经过期
            boolean isExpired = hasExpired(user);
            if (isExpired) {
                auth.setAuthenticated(false);
            } else {
                auth.setAuthenticated(true);
            }
            return auth;
        }
        return null;
    }

    public static Claims parseJWT(String token) {

        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(AuthorizedConstant.SECRET)
                    .parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return claims;
    }

    public static boolean hasExpired(LoginUser user) {
        if (null == user) {
            return true;
        }
        Long currentTime = System.currentTimeMillis();
        Long diff = currentTime - user.getCreated();
        // 5 * 60 * 60 * 1000代表5个小时
        if (diff > USER_EXPIRED_TIME) {
            return true;
        }
        return false;
    }

    public static String getHash(LoginUser loginUser) {
        return DigestUtils.md5DigestAsHex(String.format("%s_%d", loginUser.getUsername(), loginUser.getCreated()).getBytes());
    }
}
