package com.auxby.productmanager.rabbitmq;

import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final Queue queue;
    private final RabbitTemplate rabbitTemplate;

    public void send(MessagePayload message) {
        log.info("Send message : " + message);
        try {
            rabbitTemplate.convertAndSend(queue.getName(), message);
        } catch (AmqpException e) {
            log.info("Error when sending notification message: " + e.getMessage());
        }
    }
}
