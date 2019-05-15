package com.roncoo.eshop.datasync.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息发送
 */
@Component
public class RabbitMQSender {
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void send(String queue,String message){
        amqpTemplate.convertAndSend(queue,message);
    }
}
