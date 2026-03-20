package com.health.service;

import com.health.dto.AiChatRequest;
import com.health.dto.AiChatResponse;

public interface AiService {

    AiChatResponse chat(AiChatRequest request);
}
