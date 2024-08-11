package com.auxby.productmanager.rabbitmq;

import com.auxby.productmanager.rabbitmq.message.MessagePayload;
import com.auxby.productmanager.rabbitmq.message.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSenderTest {
    @InjectMocks
    private MessageSender sender;
    @Mock
    private Queue queue;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void should_SucceedSendingMessage_When_MethodIsCalled() {
        //Arrange
        var payloadMock = MessagePayload.builder()
                .messageType(MessageType.NEW_MESSAGE_RECEIVED)
                .receiver("joe.doe@gmail.com")
                .offerId(1)
                .build();
        String mockQueue = "auxby-test-queue";
        when(queue.getName()).thenReturn(mockQueue);

        //Act
        sender.send(payloadMock);

        //Assert
        verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(MessagePayload.class));
    }
}