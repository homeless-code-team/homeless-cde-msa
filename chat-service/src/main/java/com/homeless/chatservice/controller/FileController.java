package com.homeless.chatservice.controller;

import com.homeless.chatservice.dto.CommonResDto;
import com.homeless.chatservice.service.FileService;
import com.homeless.chatservice.service.ResponseService;
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
    ResponseService responseService;

    // 파일 업로드 처리
    @PostMapping("/upload")
    public ResponseEntity<CommonResDto<Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일이 비어 있는지 확인
            if (file.isEmpty()) {
                return responseService.createErrorResponse(HttpStatus.BAD_REQUEST,"File is empty");
            }

            // 파일 업로드 처리
            String fileUrl = fileService.uploadFile(file);

            // 성공 응답 생성
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            CommonResDto<Object> commonResDto = new CommonResDto<>(HttpStatus.OK, "File saved successfully", result);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);

        } catch (IOException e) {
            return responseService.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file");
        } catch (Exception e) {
            return responseService.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<CommonResDto<Object>> deleteFile(@RequestParam String fileUrl) throws Exception {
        try {
            fileService.deleteFile(fileUrl);
            CommonResDto<Object> dto = new CommonResDto<>(HttpStatus.OK, "File deleted successfully", null);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return responseService.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file");
        }
    }

}
