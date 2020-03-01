package com.my.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author pengjinguo
 * @description 网关
 * @date 2019/10/31 下午6:19
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableCaching
@ComponentScan(value = {"com.my"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
