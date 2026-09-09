package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "RABBITMQ-PUBLISHER")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.publisher", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMQNotificationPublisher implements NotificationPublisher{
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(EmailNotificationMessage message) {
        if(message == null){
            log.warn("Payload EmailNotificationMessage is null, skipping publish.");
            return;
        }

        // log chuẩn bị gửi để truy vết
        log.info("Preparing to publish email notification: messageId={}, type={}, to={}",
                message.getMessageId(), message.getNotificationType(), message.getEmailTo());

        try {
            // Gắn CorrelationData chứa messageId để phục vụ truy vết và Publisher Confirms
            CorrelationData correlationData = new CorrelationData(message.getMessageId());

            // Đẩy message qua Exchange với routing key
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.EMAIL_NOTIFICATION_ROUTING_KEY,
                    message, correlationData);

            // Log xác nhận đẩy vào pipeline thành công
            log.info("Message published successfully: messageId={}, exchange={}, routingKey={}",
                    message.getMessageId(),
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.EMAIL_NOTIFICATION_ROUTING_KEY);
        } catch (AmqpException e) {
            log.error("Failed to publish message to RabbitMQ: messageId={}, to={}, error={}",
                    message.getMessageId(),
                    message.getEmailTo(),
                    e.getMessage(), e);
            throw new RuntimeException("Failed to publish notification: messageId=" + message.getMessageId(), e);
        }
    }
}
