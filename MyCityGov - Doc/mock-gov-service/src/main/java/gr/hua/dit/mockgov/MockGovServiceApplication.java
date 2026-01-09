package gr.hua.dit.mockgov;

import gr.hua.dit.mockgov.config.MockGovProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MockGovProperties.class)
public class MockGovServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockGovServiceApplication.class, args);
    }
}
