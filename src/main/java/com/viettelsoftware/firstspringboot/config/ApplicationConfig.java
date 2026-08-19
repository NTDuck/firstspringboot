package com.viettelsoftware.firstspringboot.config;

import com.viettelsoftware.firstspringboot.config.properties.DisplayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DisplayProperties.class)
public class ApplicationConfig { }
