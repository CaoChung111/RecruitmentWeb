package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.CvParsingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "CV-PARSING-PUBLISHER")
@RequiredArgsConstructor
public class RabbitMQCvParsingPublisher implements CvParsingPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(CvParsingMessage message) {
        if(message == null){
            log.warn("CvParsingMessage is null, skipping publish.");
            return;
        }

        log.info("Preparing to publish CV parsing task: messageId={}, resumeId={}",
                message.getMessageId(), message.getResumeId());
        try {
            CorrelationData correlationData = new CorrelationData(message.getMessageId());

            rabbitTemplate.convertAndSend(RabbitMQConfig.CV_PARSING_EXCHANGE,
                    RabbitMQConfig.CV_PARSING_ROUTING_KEY, message, correlationData);

            log.info("Published CV parsing message successfully: messageId={}, resumeId={}",
                    message.getMessageId(), message.getResumeId());
        } catch (AmqpException e) {
            log.error("Failed to publish CV parsing message: messageId={}, error={}",
                    message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish CV parsing message: " + e.getMessage(), e);
        }
    }
}
