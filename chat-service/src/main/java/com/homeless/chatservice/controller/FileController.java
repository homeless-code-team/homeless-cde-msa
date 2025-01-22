package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file/chats")
@Slf4j
public class FileController {
    private final FileService fileService;

    // 파일 업로드 처리
    @PostMapping("/upload")
    public ResponseEntity<CommonResDto<Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일이 비어 있는지 확인
            if (file.isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.BAD_REQUEST, "File is empty", errorResult);
                return new ResponseEntity<>(errorResDto, HttpStatus.BAD_REQUEST);
            }

            // 파일 업로드 처리
            String fileUrl = fileService.uploadFile(file);
            log.info("File uploaded successfully: {}", fileUrl);

            // 성공 응답 생성
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "File saved successfully", result);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error while uploading file: {}", e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", errorResult);
            return new ResponseEntity<>(errorResDto, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            CommonResDto<Object> errorResDto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", errorResult);
            return new ResponseEntity<>(errorResDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<CommonResDto<Object>> deleteFile(@RequestParam String fileUrl) throws Exception {
        try {
            log.info("Delete file fileUrl {}", fileUrl);
            // 파일 삭제 서비스 호출
            fileService.deleteFile(fileUrl);
            // 삭제 성공 시 응답
            CommonResDto<Object> dto = new CommonResDto<>(HttpStatus.OK, "File deleted successfully", null);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            // 삭제 실패 시 예외 처리
            CommonResDto<Object> dto = new CommonResDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

}
