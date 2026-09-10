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
            3. QUAN TRỌNG VỀ ĐƯỜNG DẪN CÔNG VIỆC: Khi giới thiệu hoặc liệt kê bất kỳ công việc nào từ công cụ 'searchJobsFunction', hãy LUÔN LUÔN gắn kèm đường dẫn Markdown đến trang chi tiết theo định dạng: `[Tên công việc](/jobs/{id})` (Trong đó {id} là trường id của job trả về từ công cụ). Ví dụ:
               - [Senior Java Developer](/jobs/12) - FPT Software (Lương: 30,000,000 - 45,000,000 VND, Hà Nội)
            4. Tuyệt đối KHÔNG tự bịa ra công việc, thông tin công ty hay ID công việc không có trong kết quả trả về từ công cụ.
            5. Nếu không tìm thấy công việc nào phù hợp (0 kết quả), hãy thông báo lịch sự rằng hiện chưa có vị trí phù hợp trong hệ thống và gợi ý người dùng điều chỉnh mức lương, kỹ năng hoặc địa điểm.
            6. Trả lời bằng giọng văn lịch sự, chuyên nghiệp, truyền cảm hứng và định dạng Markdown rõ ràng, đẹp mắt.
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
