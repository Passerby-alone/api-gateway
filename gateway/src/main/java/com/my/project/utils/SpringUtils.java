package com.my.project.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/29 下午6:18
 */
@Component
public class SpringUtils implements ApplicationContextAware, InitializingBean {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return SpringUtils.applicationContext;
    }

    public static Object getBean(String name) {

        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clz) {
        return getApplicationContext().getBean(clz);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

//        Object obj = getBean(RequestRateLimiterGatewayFilterFactory.class);
//        System.out.println(obj);
    }
}
