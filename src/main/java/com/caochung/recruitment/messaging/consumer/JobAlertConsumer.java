package com.caochung.recruitment.messaging.consumer;

import com.caochung.recruitment.config.RabbitMQConfig;
import com.caochung.recruitment.messaging.dto.JobAlertMessage;
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

@Component
@Slf4j(topic = "JOB-ALERT-CONSUMER")
@RequiredArgsConstructor
public class JobAlertConsumer {
    private final EmailService emailService;

    @RabbitListener(
            queues = RabbitMQConfig.JOB_ALERT_EMAIL_QUEUE,
            ackMode = "MANUAL",
            containerFactory = "jobAlertContainerFactory")
    public void consume(@Payload JobAlertMessage message,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        Channel channel) throws IOException {
        log.info("Received email message: id={}, to={}",
                message.getMessageId(), message.getSubscriberEmail());

        try {
            processJobAlert(message);
            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed and ACKed message: id={}, tag={}",
                    message.getMessageId(), deliveryTag);
        }catch (Exception e){
            log.error("Failed to process email message: id={}, tag={}, error={}",
                    message.getMessageId(), deliveryTag, e.getMessage(), e);

            try {
                channel.basicNack(deliveryTag, false, false);
                log.warn("NACKed message with requeue=false (routed to DLQ): id={}, tag={}",
                        message.getMessageId(), deliveryTag);
            } catch (IOException ex) {
                log.error("Failed to NACK message to RabbitMQ: id={}, tag={}, error={}",
                        message.getMessageId(), deliveryTag, ex.getMessage(), ex);
            }
        }

    }

    private void processJobAlert(JobAlertMessage message) {
        if (message.getMatchedJobs() == null || message.getMatchedJobs().isEmpty()) {
            throw new IllegalArgumentException(
                    "JobAlertMessage id=" + message.getMessageId() + " has empty job list");
        }
        emailService.sendJobAlertEmail(message.getSubscriberEmail(), message.getSubscriberName(), message.getMatchedJobs());
    }
}
