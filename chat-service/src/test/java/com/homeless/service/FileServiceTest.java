package com.homeless.service;

import com.homeless.chatservice.common.config.AwsS3Config;
import com.homeless.chatservice.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FileServiceTest {

    @Mock
    private AwsS3Config awsS3Config;

    @InjectMocks
    private FileService fileService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Mock MultipartFile 생성
        mockFile = Mockito.mock(MultipartFile.class);
        try {
            Mockito.when(mockFile.getOriginalFilename()).thenReturn("testfile.txt");
            Mockito.when(mockFile.getBytes()).thenReturn("Hello, World!".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUploadFile() throws Exception {
        // S3에 업로드된 파일 URL 예시
        String expectedFileUrl = "https://s3.amazonaws.com/" + bucketName + "/testfile.txt";

        // AWS S3 업로드 메서드가 호출될 때 예상되는 동작
        Mockito.when(awsS3Config.uploadToS3Bucket(Mockito.any(), Mockito.anyString()))
                .thenReturn(expectedFileUrl);

        // uploadFile 메서드 호출
        String actualFileUrl = fileService.uploadFile(mockFile);

        // 결과 검증
        assertEquals(expectedFileUrl, actualFileUrl, "File URL should match the expected URL.");
    }

    @Test
    void testDeleteFile() throws Exception {
        // 삭제할 파일 URL
        String fileUrl = "https://s3.amazonaws.com/" + bucketName + "/testfile.txt";

        // 파일 삭제 메서드가 호출될 때 예상되는 동작
        Mockito.doNothing().when(awsS3Config).deleteFromS3Bucket(Mockito.anyString());

        // deleteFile 메서드 호출
        fileService.deleteFile(fileUrl);

        // 메서드가 예외 없이 호출되었으므로, 정상적으로 테스트가 종료됩니다.
    }
}
