package com.caochung.recruitment.ai.service;

import com.caochung.recruitment.ai.dto.ChatRequestDTO;

public interface AiChatbotService {
    String chatWithAssistant(ChatRequestDTO request);
}
