package gr.hua.dit.mycitygov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Χαμηλά timeouts ώστε external dependencies (NOC/MockGov/etc.) να μην "κολλάνε" το UI.
        // (ms)
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(4000);
        return new RestTemplate(factory);
    }
}
