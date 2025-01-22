package com.homeless.chatservice.service;

import com.homeless.chatservice.common.config.AwsS3Config;
import com.homeless.chatservice.entity.ChatMessage;
import com.homeless.chatservice.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final AwsS3Config awsS3Config;
    private final ChatMessageRepository chatMessageRepository;



    @Transactional
    public String uploadFile(MultipartFile file) throws IOException {
        // 파일 이름을 UUID로 생성하여 고유하게 처리 (필요 시 UUID 대신 다른 방식 사용 가능)
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // 파일을 S3에 업로드하고 URL을 반환
        byte[] fileBytes = file.getBytes();

        return awsS3Config.uploadToS3Bucket(fileBytes, fileName); // 업로드된 파일의 URL 반환
    }

    // 파일 삭제 처리 메서드 (파일 URL을 이용해 삭제)
    public void deleteFile(String fileUrl) throws Exception {
        awsS3Config.deleteFromS3Bucket(fileUrl);
    }

    public void deleteChatMessagesWithFileByChannelId(String channelId) throws Exception {
        List<ChatMessage> deletingChatMessagesWithFile = chatMessageRepository.findByChannelIdAndFileUrlIsNotNull(channelId);
        log.debug("Deleting chat messages {}", deletingChatMessagesWithFile);
        for (ChatMessage chat : deletingChatMessagesWithFile) {
            try {
                awsS3Config.deleteFromS3Bucket(chat.getFileUrl());
                log.info("Successfully deleted file: {}", chat.getFileUrl());
            } catch (Exception e) {
                log.error("Failed to delete file: {}", chat.getFileUrl(), e);
            }
        }
    }
}
