package gr.hua.dit.noc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 *
 * @author Dimitris Gkoulis
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("NOC Services")
                        .version("v1")
                        .description("Stateless REST API for lookups and notifications"));
    }

    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("api")
                .packagesToScan("gr.hua.dit.noc.web.rest")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
