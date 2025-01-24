package com.homeless.chatservice.service;


import com.homeless.chatservice.dto.ChatMessageResponse;
import com.homeless.chatservice.dto.CommonResDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResponseService {

    public Map<String, Object> createMessageResultMap(Page<ChatMessageResponse> messages) {
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages.getContent());
        result.put("totalElements", messages.getTotalElements());
        result.put("totalPages", messages.getTotalPages());
        result.put("currentPage", messages.getNumber());
        result.put("isLast", messages.isLast());
        return result;
    }

    public ResponseEntity<CommonResDto<Object>> createErrorResponse(HttpStatus status, String message) {
        CommonResDto<Object> errorResponse = new CommonResDto<>(status, message, null);
        return new ResponseEntity<>(errorResponse, status);
    }


}
