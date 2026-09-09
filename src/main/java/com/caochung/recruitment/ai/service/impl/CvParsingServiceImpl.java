package com.caochung.recruitment.ai.service.impl;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;
import com.caochung.recruitment.ai.service.CvParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "GEMINI-CV-PARSING")
@RequiredArgsConstructor
public class CvParsingServiceImpl implements CvParsingService {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            Bạn là một chuyên gia phân tích CV và tuyển dụng nhân sự chuyên nghiệp.
                    Nhiệm vụ của bạn là đọc kỹ nội dung trong file CV PDF được cung cấp và trích xuất thông tin một cách trung thực, chính xác nhất.
                    Quy tắc nghiêm ngặt:
                    1. Chỉ trích xuất thông tin CÓ TRONG CV. Tuyệt đối KHÔNG suy đoán, KHÔNG tự bịa kỹ năng hay kinh nghiệm.
                    2. Nếu một trường thông tin không tìm thấy trong CV, hãy để giá trị null.
                    3. Danh sách skills phải là các từ khóa công nghệ, kỹ năng cụ thể (VD: Java, Spring Boot, ReactJS, Docker, AWS,...).
            """;

    @Override
    public ParsedCvDTO parse(byte[] pdfBytes) {
        log.info("CV PARSING STARTED (size: {})", pdfBytes.length);

        BeanOutputConverter<ParsedCvDTO> converter = new BeanOutputConverter<>(ParsedCvDTO.class);
        try{
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT + "\n" + converter.getFormat())
                    .user(userSpec -> userSpec.text("Hãy phân tích file CV đính kèm và trích xuất thông tin theo định dạng JSON được yêu cầu:")
                            .media(MediaType.APPLICATION_PDF, new ByteArrayResource(pdfBytes)))
                    .call()
                    .content();

            log.info("Gemini parsing completed successfully. Converting response to ParsedCvDTO");
            return converter.convert(response);
        }catch (Exception e){
            log.error("Failed to parse CV with Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini CV parsing failed: "+ e.getMessage(), e);
        }
    }
}
