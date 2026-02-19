package com.java.test.junior.configuration;

import com.java.test.junior.interceptor.CopyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisInterceptorConfig {

    @Bean
    public CopyInterceptor CopyInterceptor() {
        return new CopyInterceptor();
    }
}