package com.health.controller;

import com.health.annotation.Log;
import com.health.common.Result;
import com.health.dto.AiChatRequest;
import com.health.dto.AiChatResponse;
import com.health.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Chat", description = "AI health consultation")
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Log("AI chat")
    @Operation(summary = "Send message to AI health assistant")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiService.chat(request));
    }
}
