package com.my.project.config;

import com.my.project.security.AuthorizedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/24 下午9:32
 */
@Configuration
public class AuthorizedConfig {

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    @Bean
    public AuthorizedService authorizedService() {
        return new AuthorizedService(redisTemplate);
    }
}
