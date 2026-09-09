package com.caochung.recruitment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Direct Exchange Constants
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String EMAIL_NOTIFICATION_QUEUE = "email-notification-queue";
    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification";
    public static final String NOTIFICATION_DLX = "notification.dlx";
    public static final String EMAIL_NOTIFICATION_DLQ = "email-notification-dlq";
    public static final String EMAIL_NOTIFICATION_DLQ_ROUTING_KEY = "email.notification.dead";

    // Fanout Exchange Constants
    public static final String JOB_ALERT_FANOUT_EXCHANGE = "job-alert.fanout";
    public static final String JOB_ALERT_EMAIL_QUEUE = "job-alert-email-queue";
    public static final String JOB_ALERT_DLX = "job-alert.dlx";
    public static final String JOB_ALERT_EMAIL_DLQ = "job-alert-email-dlq";

    // Direct Exchange Constants for CV Parsing
    public static final String CV_PARSING_EXCHANGE = "cv-parsing.exchange";
    public static final String CV_PARSING_QUEUE = "cv-parsing-queue";
    public static final String CV_PARSING_ROUTING_KEY = "cv.parse";
    public static final String CV_PARSING_DLX = "cv-parsing.dlx";
    public static final String CV_PARSING_DLQ = "cv-parsing-dlq";
    public static final String CV_PARSING_DLQ_ROUTING_KEY = "cv.parse.dead";


    /**
     * Main Direct Exchange (Durable = true để tồn tại khi broker restart)
     */
    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_DLX)
                .durable(true)
                .build();
    }

    // Queue chính
    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", EMAIL_NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    // DLQ chứa message lỗi
    @Bean
    public Queue emailNotificationDLQ() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_DLQ).build();
    }

    // Binding Queue chính vào Main Exchange qua Routing Key
    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);
    }

    // Binding DLQ vào DLX
    @Bean
    public Binding emailNotificationDLQBinding() {
        return BindingBuilder.bind(emailNotificationDLQ())
                .to(notificationDeadLetterExchange())
                .with(EMAIL_NOTIFICATION_DLQ_ROUTING_KEY);
    }

    /**
     * Fanout Exchange
     */
    @Bean
    public FanoutExchange jobAlertFanoutExchange() {
        return ExchangeBuilder.fanoutExchange(JOB_ALERT_FANOUT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public FanoutExchange jobAlertDeadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(JOB_ALERT_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue jobAlertEmailQueue() {
        return QueueBuilder.durable(JOB_ALERT_EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", JOB_ALERT_DLX)
                .build();
    }

    @Bean
    public Queue jobAlertEmailDLQ() {
        return QueueBuilder.durable(JOB_ALERT_EMAIL_DLQ).build();
    }

    @Bean
    public Binding jobAlertEmailBinding() {
        return BindingBuilder.bind(jobAlertEmailQueue())
                .to(jobAlertFanoutExchange());
    }

    @Bean
    public Binding jobAlertEmailDLQBinding() {
        return BindingBuilder.bind(jobAlertEmailDLQ())
                .to(jobAlertDeadLetterExchange());
    }

    // Factory riêng cho job-alert vì throughput cao hơn
    @Bean
    public SimpleRabbitListenerContainerFactory jobAlertContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(3); // Tối thiểu 3 threads luôn chạy
        factory.setMaxConcurrentConsumers(10); // Tự scale lên tối đa là 10
        factory.setPrefetchCount(5); // Mỗi thread giữ 5 message
        return factory;
    }

    /**
     * Direct Exchange CV parse
     */
    @Bean
    public DirectExchange cvParsingExchange() {
        return ExchangeBuilder.directExchange(CV_PARSING_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange cvParsingDeadLetterExchange() {
        return ExchangeBuilder.directExchange(CV_PARSING_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue cvParsingQueue() {
        return QueueBuilder.durable(CV_PARSING_QUEUE)
                .withArgument("x-dead-letter-exchange", CV_PARSING_DLX)
                .withArgument("x-dead-letter-routing-key", CV_PARSING_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue cvParsingDLQ() {
        return QueueBuilder.durable(CV_PARSING_DLQ).build();
    }

    @Bean
    public Binding cvParsingBinding() {
        return BindingBuilder.bind(cvParsingQueue())
                .to(cvParsingExchange()).with(CV_PARSING_ROUTING_KEY);
    }

    @Bean
    public Binding cvParsingDLQBinding() {
        return BindingBuilder.bind(cvParsingDLQ())
                .to(cvParsingDeadLetterExchange()).with(CV_PARSING_DLQ_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory cvParsingContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(2);
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


}
