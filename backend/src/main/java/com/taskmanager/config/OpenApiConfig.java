package com.taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata used to render the Swagger UI.
 *
 * <p>The UI is available at <a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a>.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Task Manager API")
                .description("REST API to manage personal tasks")
                .version("1.0.0")
                .contact(new Contact().name("Task Manager"))
                .license(new License().name("MIT")));
    }
}
