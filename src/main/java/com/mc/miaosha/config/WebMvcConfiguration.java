package com.mc.miaosha.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 解决前后端分离开发是ajax跨域请求失败的问题
 * When allowCredentials is true, allowedOrigins cannot contain
 * the special value "*" since that cannot be set on the "Access-Control-Allow-Origin" response header.
 * To allow credentials to a set of origins, list them explicitly or
 * consider using "allowedOriginPatterns" instead.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                //.allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}
