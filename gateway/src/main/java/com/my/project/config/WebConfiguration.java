//package com.my.project.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//
///**
// * @author pengjinguo
// * @description TODO
// * @date 2019/11/13 下午3:52
// */
//@Configuration
//@ControllerAdvice
//public class WebConfiguration extends WebMvcConfigurationSupport {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
////    @Bean
////    public TokenAuthenticationService generateAuthenticationService() {
////        TokenAuthenticationService authenticationService = new TokenAuthenticationService(redisTemplate, SecurityConstants.SIGN_SECRET);
////        return authenticationService;
////    }
////
////    @Bean
////    public AuthenticationProviderImpl provider() {
////        return new AuthenticationProviderImpl(redisTemplate);
////    }
//}
