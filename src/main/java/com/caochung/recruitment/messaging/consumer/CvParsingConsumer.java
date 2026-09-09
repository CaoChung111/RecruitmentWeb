package com.caochung.recruitment.messaging.consumer;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;
import com.caochung.recruitment.ai.service.CvParsingService;
import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.CvParsingMessage;
import com.caochung.recruitment.service.ResumeDetailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
@Slf4j(topic = "CV-PARSING-CONSUMER")
@RequiredArgsConstructor
public class CvParsingConsumer {

    private final CvParsingService cvParsingService;
    private final RestTemplate restTemplate;
    private final ResumeDetailService resumeDetailService;

    @RabbitListener(
            queues = RabbitMQConfig.CV_PARSING_QUEUE,
            ackMode = "MANUAL",
            containerFactory = "cvParsingContainerFactory")
    public void consume(@Payload CvParsingMessage message,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        Channel channel) throws IOException {
        log.info("Received CV parsing request: messageId={}, resumeId={}",
                message.getMessageId(), message.getResumeId());
        try {
            resumeDetailService.markAsProcessing(message.getResumeId());

            // Tải PDF từ cloudinary
            byte[] pdfBytes = restTemplate.getForObject(message.getCloudinaryUrl(), byte[].class);
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new IllegalArgumentException("Cannot download PDF from URL: "+ message.getCloudinaryUrl());
            }

            // Gọi AI
            ParsedCvDTO parsedCv = cvParsingService.parse(pdfBytes);

            // Lưu vào resume_details
            resumeDetailService.saveParsedResult(message.getResumeId(), parsedCv);

            // Thành công -> ACK
            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed and ACKed CV parsing: resumeId={}", message.getResumeId());

        }catch (Exception e) {
            log.error("Failed to parse CV: messageId={}, resumeId={}", message.getMessageId(), message.getResumeId(), e);
            try {
                resumeDetailService.markAsFailed(message.getResumeId());
                channel.basicNack(deliveryTag, false, false);
            }catch (IOException ioe){
                log.error("Failed to NACK message: {}", ioe.getMessage());
            }
        }
    }
}
