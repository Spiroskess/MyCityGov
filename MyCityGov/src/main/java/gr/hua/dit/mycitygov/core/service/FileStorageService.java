package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.config.S3StorageProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;

@Service
public class FileStorageService {

    private final S3Client s3;
    private final S3StorageProperties props;

    public FileStorageService(S3Client s3, S3StorageProperties props) {
        this.s3 = s3;
        this.props = props;
        ensureBucketExists(); // init: σιγουρεύεται ότι υπάρχει bucket στο S3/MinIO
    }

    private void ensureBucketExists() {
        // Δημιουργεί bucket αν δεν υπάρχει
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(props.getBucket()).build());
        } catch (Exception e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(props.getBucket()).build());
        }
    }

    public void put(String key, InputStream inputStream, long sizeBytes, String contentType) {
        // Upload αρχείου στο S3/MinIO με key
        String ct = (contentType == null || contentType.isBlank())
            ? "application/octet-stream"
            : contentType;

        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(ct)
                .build(),
            RequestBody.fromInputStream(inputStream, sizeBytes)
        );
    }

    public ResponseInputStream<GetObjectResponse> get(String key) {
        // Download αρχείου από S3/MinIO και επιστρέφει stream
        return s3.getObject(
            GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build()
        );
    }

}
