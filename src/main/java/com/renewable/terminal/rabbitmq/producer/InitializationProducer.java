package com.renewable.terminal.rabbitmq.producer;

import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.pojo.InitializationInclination;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@Component("InitializationProducer")
@Slf4j
public class InitializationProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	// 从声音模块开始，简化消息机制（不需要使用复杂的路由规则）
	// 发送传感器数据，全部采用List
	// inclinationTotal 相关配置
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-initialization-inclination-terminal2centcontrol";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE = "queue-initialization-inclination-terminal2centcontrol";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE)
	))
	public void sendInitializationInclinationList(String initializationInclinationListStr) {

		log.info("InitializationProducer/sendInitializationInclinationList has sended: {}", initializationInclinationListStr);

		amqpTemplate.convertAndSend(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE, initializationInclinationListStr);
	}

	public void sendInitializationInclinationList(List<InitializationInclination> initializationInclinationList) {
		this.sendInitializationInclinationList(JsonUtil.obj2String(initializationInclinationList));
	}

}