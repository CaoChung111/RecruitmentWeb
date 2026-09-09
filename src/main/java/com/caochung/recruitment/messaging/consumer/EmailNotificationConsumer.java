package com.caochung.recruitment.messaging.consumer;

import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;
import com.caochung.recruitment.messaging.dto.NotificationType;
import com.caochung.recruitment.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumer lắng nghe và xử lý tin nhắn gửi email từ RabbitMQ.
 * Sử dụng Manual ACK đảm bảo không mất tin nhắn nếu worker crash giữa chừng.
 */
@Component
@Slf4j(topic = "EMAIL-CONSUMER")
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE,
            ackMode = "MANUAL",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(@Payload EmailNotificationMessage message,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        Channel channel) throws IOException {
        log.info("Received email message: id={}, type={}, to={}",
                message.getMessageId(), message.getNotificationType(), message.getEmailTo());

        try {
            // Phân loại nghiệp vụ dựa trên NotificationType
            processNotification(message);

            // Thành công -> gửi ack về rabbitMQ để xóa message khỏi queue
            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed and ACKed message: id={}, tag={}",
                    message.getMessageId(), deliveryTag);
        } catch (Exception e) {
            log.error("Failed to process email message: id={}, tag={}, error={}",
                    message.getMessageId(), deliveryTag, e.getMessage(), e);

            // Thất bại -> gửi nack và đẩy sang Dead letter queue để tránh bị lặp vô hạn
            try {
                channel.basicNack(deliveryTag, false, false);
                log.warn("NACKed message with requeue=false (routed to DLQ): id={}, tag={}",
                        message.getMessageId(), deliveryTag);
            } catch (IOException ioException) {
                log.error("Failed to NACK message to RabbitMQ: id={}, tag={}, error={}",
                        message.getMessageId(), deliveryTag, ioException.getMessage(), ioException);
            }
        }

    }

    /**
     * Dispatcher logic gọi các hàm tương ứng của EmailService
     */
    private void processNotification(EmailNotificationMessage message) {
        NotificationType type = message.getNotificationType();
        if (type == null) {
            throw new IllegalArgumentException(
                    "NotificationType is null for message: id=" + message.getMessageId()
            );
        }
        emailService.sendNotificationEmail(message);
    }
}
