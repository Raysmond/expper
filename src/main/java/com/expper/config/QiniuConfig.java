package com.expper.config;

import com.qiniu.util.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Raysmond<i@raysmond.com>
 */
@Configuration
public class QiniuConfig {
    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Bean
    public Auth qiniuAuth() {
        return Auth.create(
            jHipsterProperties.getQiniu().getAccessKey(),
            jHipsterProperties.getQiniu().getSecretKey());
    }
}
