package com.auxby.productmanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Data
@Component
@ConfigurationProperties(prefix = "web-client")
public class WebClientProps {
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String categoryUrl;
}
