package com.homeless.chatservice.service;


import com.homeless.chatservice.dto.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapService {

    public Map<String, Object> createMessageResultMap(Page<ChatMessageResponse> messages) {
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages.getContent());
        result.put("totalElements", messages.getTotalElements());
        result.put("totalPages", messages.getTotalPages());
        result.put("currentPage", messages.getNumber());
        result.put("isLast", messages.isLast());
        return result;
    }
}
