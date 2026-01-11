package gr.hua.dit.mycitygov.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfig {

    @Bean
    public S3Client s3Client(S3StorageProperties p) {
        // Δημιουργεί S3 client (π.χ. για MinIO)
        if (p.getEndpoint() == null || p.getEndpoint().isBlank()) {
            throw new IllegalStateException("Λείπει mycitygov.storage.s3.endpoint στο application.yml");
        }
        if (p.getRegion() == null || p.getRegion().isBlank()) {
            throw new IllegalStateException("Λείπει mycitygov.storage.s3.region στο application.yml");
        }
        if (p.getBucket() == null || p.getBucket().isBlank()) {
            throw new IllegalStateException("Λείπει mycitygov.storage.s3.bucket στο application.yml");
        }

        return S3Client.builder()
            .httpClientBuilder(ApacheHttpClient.builder())
            .endpointOverride(URI.create(p.getEndpoint()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(p.getAccessKey(), p.getSecretKey())
                )
            )
            .region(Region.of(p.getRegion()))
            .forcePathStyle(true)
            .build();
    }
}
