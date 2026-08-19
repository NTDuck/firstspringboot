package com.viettelsoftware.firstspringboot.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.display")
public class DisplayProperties {
    private String nullValue;
}
