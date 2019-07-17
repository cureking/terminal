package com.renewable.terminal.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.renewable.terminal.pojo.Warning;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author: jarry
 */
@Component("WarningProducer")
@Slf4j
public class WarningProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	private static final String WARNING_TERMINAL2CENTCONTROL_EXCHANGE = "warning-exchange-terminal2centcontrol";
	private static final String WARNING_TERMINAL2CENTCONTROL_QUEUE = "warning-inclination-queue-terminal2centcontrol";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(WARNING_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(WARNING_TERMINAL2CENTCONTROL_EXCHANGE)
	))
	public void sendWarningList(String warningListStr) {

		log.info("WarningProducer/sendWarningList has sended: {}", warningListStr);

		amqpTemplate.convertAndSend(WARNING_TERMINAL2CENTCONTROL_QUEUE, warningListStr);
	}

	public void sendWarningList(List<Warning> warningList) {
		this.sendWarningList(JsonUtil.obj2String(warningList));
	}
}
