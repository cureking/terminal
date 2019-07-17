package com.renewable.terminal.rabbitmq.producer;

import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.pojo.InclinationDealedTotal;
import com.renewable.terminal.rabbitmq.pojo.InclinationInit;
import com.renewable.terminal.rabbitmq.pojo.InclinationTotal;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@Component("InclinationProducer")
@Slf4j
public class InclinationProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	// 从声音模块开始，简化消息机制（不需要使用复杂的路由规则）
	// 发送传感器数据，全部采用List
	// inclinationTotal 相关配置
	private static final String INCLINATION_TOTAL_EXCHANGE = "exchange-inclination-total-data";
	private static final String INCLINATION_TOTAL_QUEUE = "queue-inclination-total-data";
	// inclinationInit 相关配置
	private static final String INCLINATION_INIT_EXCHANGE = "exchange-inclination-init-data";
	private static final String INCLINATION_INIT_QUEUE = "queue-inclination-init-data";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(INCLINATION_TOTAL_QUEUE),
			exchange = @Exchange(INCLINATION_TOTAL_EXCHANGE)
	))
	public void sendInclinationTotalList(String inclinationTotalListStr) {

		log.info("InclinationProducer/sendInclinationTotalList has sended: {}", inclinationTotalListStr);

		amqpTemplate.convertAndSend(INCLINATION_TOTAL_QUEUE, inclinationTotalListStr);
	}

	public void sendInclinationTotalList(List<InclinationTotal> inclinationTotalList) {
		this.sendInclinationTotalList(JsonUtil.obj2String(inclinationTotalList));
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(INCLINATION_INIT_QUEUE),
			exchange = @Exchange(INCLINATION_INIT_EXCHANGE)
	))
	public void sendInclinationInitList(String inclinationTotalListStr) {

		log.info("InclinationProducer/sendInclinationInitList has sended: {}", inclinationTotalListStr);

		amqpTemplate.convertAndSend(INCLINATION_INIT_QUEUE, inclinationTotalListStr);
	}

	public void sendInclinationInitList(List<InclinationInit> inclinationInitList) {
		this.sendInclinationInitList(JsonUtil.obj2String(inclinationInitList));
	}
}