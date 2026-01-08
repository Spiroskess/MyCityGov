package gr.hua.dit.mycitygov.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NocProperties.class, MockGovProperties.class})
public class RestApiClientConfig { }
