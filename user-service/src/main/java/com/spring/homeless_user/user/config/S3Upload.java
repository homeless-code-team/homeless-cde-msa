package com.spring.homeless_user.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component // Spring Bean으로 등록
public class S3Upload {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    // 생성자 주입 방식으로 AWS 설정 값 초기화
    public S3Upload(
            @Value("${cloud.aws.credentials.access-key}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${cloud.s3.bucket-name}") String bucketName,
            @Value("${cloud.aws.region.static}") String region) {
        this.bucketName = bucketName;
        this.region = region;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * S3에 파일 업로드
     * @param file 업로드할 MultipartFile
     * @return 업로드된 파일의 URL
     * @throws IOException 파일 처리 오류 시 예외
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        // URL 생성
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
