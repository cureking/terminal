package com.renewable.terminal.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.pojo.Terminal;
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
@Component("TerminalProducer")
@Slf4j
public class TerminalProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-terminal-config-terminal2centcontrol";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE = "queue-terminal-config-terminal2centcontrol";

	// 初始化后台相关配置（如队列等）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(value = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE)
	))
	public void sendTerminal(String terminalStr){

		log.info("TerminalProducer/sendTerminal has sended: {}", terminalStr);

		amqpTemplate.convertAndSend(TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE, terminalStr);
	}

	public void sendTerminal(Terminal terminal) {
		this.sendTerminal(JsonUtil.obj2String(terminal));
	}
}
