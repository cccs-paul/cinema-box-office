/*
 * myRC - Web MVC Configuration
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for custom interceptors.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final InactiveFiscalYearInterceptor inactiveFiscalYearInterceptor;

    public WebMvcConfig(InactiveFiscalYearInterceptor inactiveFiscalYearInterceptor) {
        this.inactiveFiscalYearInterceptor = inactiveFiscalYearInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(inactiveFiscalYearInterceptor)
                .addPathPatterns(
                        "/responsibility-centres/*/fiscal-years/**",
                        "/fiscal-years/**"
                );
    }
}
