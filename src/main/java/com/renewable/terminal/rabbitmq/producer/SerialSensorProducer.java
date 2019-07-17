package com.renewable.terminal.rabbitmq.producer;

import com.renewable.terminal.pojo.SerialSensor;
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

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@Component("SerialSensorProducer")
@Slf4j
public class SerialSensorProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-serial-sensor-terminal2centcontrol";
	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE = "queue-serial-sensor-terminal2centcontrol";

	// 初始化后台相关配置（如队列等）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(value = SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE)
	))
	public void sendSerialSensor(String serialSensorListStr){

		log.info("SerialSensorProducer/sendSerialSensor has sended: {}", serialSensorListStr);

		amqpTemplate.convertAndSend(SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE, serialSensorListStr);
	}

	public void sendSerialSensor(List<SerialSensor> serialSensorList) throws Exception {
		this.sendSerialSensor(JsonUtil.obj2String(serialSensorList));
	}
}
