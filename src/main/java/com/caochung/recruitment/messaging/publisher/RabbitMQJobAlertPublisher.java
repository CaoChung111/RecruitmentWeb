package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.JobAlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "JOB-ALERT-PUBLISHER")
@RequiredArgsConstructor
public class RabbitMQJobAlertPublisher implements JobAlertPublisher
{
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(JobAlertMessage message) {
        if (message == null) {
            log.warn("Payload JobAlertMessage is null, skipping publish.");
            return;
        }

        log.info("Preparing to publish job alert message: messageId={}, to={}",
                message.getMessageId(), message.getSubscriberEmail());

        try {
            CorrelationData correlationData = new CorrelationData(message.getMessageId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.JOB_ALERT_FANOUT_EXCHANGE,
                    "", message, correlationData);
            log.info("Message published successfully: messageId={}, exchange={}, to={}, jobs={}", message.getMessageId(),
                    RabbitMQConfig.JOB_ALERT_FANOUT_EXCHANGE,
                    message.getSubscriberEmail(),
                    message.getMatchedJobs().size());
        } catch (AmqpException e) {
            log.error("Failed to publish message to RabbitMQ: messageId={}, to={}, error={}",
                    message.getMessageId(), message.getSubscriberEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish notification: messageId=" + message.getMessageId(), e);
        }
    }
}
