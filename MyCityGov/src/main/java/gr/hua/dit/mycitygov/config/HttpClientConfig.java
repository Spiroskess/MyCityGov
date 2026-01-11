package gr.hua.dit.mycitygov.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class HttpClientConfig {
    /**
     * RestTemplate bean για HTTP κλήσεις προς εξωτερικές υπηρεσίες
     * (π.χ. MockGov / SMS service), ώστε να γίνεται injection όπου χρειάζεται.
     */

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
