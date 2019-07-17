package com.renewable.terminal.rabbitmq.producer;

import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.pojo.SensorRegister;
import com.renewable.terminal.util.JsonUtil;
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
@Component("SensorRegisterProducer")
public class SensorRegisterProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	// 从声音模块开始，简化消息机制（不需要使用复杂的路由规则）
	// 发送传感器数据，全部采用List
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-sensor-register-terminal2centcontrol";
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE = "queue-sensor-register-terminal2centcontrol";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE)
	))
	public void sendSensorRegisterList(String sensorRegisterListStr) {
		amqpTemplate.convertAndSend(SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE, sensorRegisterListStr);
	}

	public void sendSensorRegisterList(List<SensorRegister> sensorRegisterList) {
		this.sendSensorRegisterList(JsonUtil.obj2String(sensorRegisterList));
	}
}