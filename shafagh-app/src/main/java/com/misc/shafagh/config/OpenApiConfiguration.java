package com.misc.shafagh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One OpenAPI document for all REST controllers loaded by the application. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI shafaghOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Shafagh API")
                .description("Shared REST API documentation for all Shafagh modules")
                .version("1.0.0"));
    }
}
