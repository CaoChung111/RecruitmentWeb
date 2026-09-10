package com.caochung.recruitment.ai.service.impl;

import com.caochung.recruitment.ai.dto.ChatRequestDTO;
import com.caochung.recruitment.ai.service.AiChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CHATBOT-SERVICE")
public class AiChatbotServiceImpl implements AiChatbotService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private static final String SYSTEM_PROMPT = """
            Bạn là Trợ lý Tuyển dụng AI Cao cấp của nền tảng RecruitmentWeb.
            Tên của bạn là 'RecruitAI'.
            
            NHIỆM VỤ CỦA BẠN:
            1. Tư vấn, giải đáp thắc mắc của ứng viên và nhà tuyển dụng về các cơ hội nghề nghiệp.
            2. Khi người dùng hỏi về danh sách công việc, hãy SỬ DỤNG CÔNG CỤ (Function Calling) 'searchJobsFunction' hoặc 'jobDetailFunction' để lấy dữ liệu thực tế từ hệ thống.
            3. Tuyệt đối KHÔNG tự bịa ra công việc hoặc thông tin công ty không có trong cơ sở dữ liệu.
            4. Trả lời bằng giọng văn lịch sự, chuyên nghiệp, truyền cảm hứng và định dạng Markdown đẹp mắt.
            """;

    @Override
    public String chatWithAssistant(ChatRequestDTO request) {
        String conversationId = (request.conversationId() != null && !request.conversationId().isBlank())
                ? request.conversationId() : "default_session";
        log.info("Processing chat message for conversationId={}: {}", request.conversationId(), request.message());
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(request.message())
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisorSpec -> advisorSpec.param("chat_memory_conversation_id", conversationId))
                .toolNames("searchJobsFunction", "jobDetailFunction")
                .call()
                .content();
    }
}
