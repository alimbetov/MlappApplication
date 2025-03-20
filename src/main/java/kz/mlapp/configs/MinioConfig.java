package kz.mlapp.configs;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.pool-size}")
    private int poolSize;

    @Bean
    public MinioClient minioClient() {
        // Создаем пул соединений для MinIO
        ConnectionPool connectionPool = new ConnectionPool(poolSize, 5, TimeUnit.MINUTES);

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(new okhttp3.OkHttpClient.Builder()
                        .connectionPool(connectionPool)
                        .build())
                .build();
    }
}
