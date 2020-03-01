package com.my.project.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/29 上午11:37
 */
@Configuration
public class RateLimiterConfiguration {

    @Bean(value = "userServiceKeyResolver")
    public KeyResolver userServiceKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}

