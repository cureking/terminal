package com.renewable.terminal.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author: jarry
 */
@Component("SerialSensorProducer")
public class SerialSensorProducer {
//
//	private static String rabbitmqHost = "47.92.249.250";
//	private static String rabbitmqUser = "admin";
//	private static String rabbitmqPassword = "123456";
//	private static String rabbitmqPort = "5672";
//
//	private static final String IP_ADDRESS = rabbitmqHost;
//	private static final int PORT = Integer.parseInt(rabbitmqPort);
//	private static final String USER_NAME = rabbitmqUser;
//	private static final String USER_PASSWORD = rabbitmqPassword;
//
//
//	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-serial-sensor-terminal2centcontrol";
//	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE = "queue-serial-sensor-terminal2centcontrol";
//	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
//	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_BINDINGKEY = "serial.sensor.terminal2centcontrol";
//	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINGKEY = "serial.sensor.terminal2centcontrol";
//
//	public static void sendSerialSensor(List<SerialSensor> serialSensorList) throws IOException, TimeoutException, InterruptedException {
//
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setHost(IP_ADDRESS);
//		factory.setPort(PORT);
//		factory.setUsername(USER_NAME);
//		factory.setPassword(USER_PASSWORD);
//
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
//		channel.exchangeDeclare(SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE, SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINETYPE, true, false, null);
//		channel.queueDeclare(SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE, true, false, false, null);
//		channel.queueBind(SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE, SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE, SERIAL_SENSOR_TERMINAL2CENTCONTROL_BINDINGKEY);
//
//		String serialSensorListStr = JsonUtil.obj2StringPretty(serialSensorList);
//		channel.basicPublish(SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE, SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, serialSensorListStr.getBytes());
//
//		channel.close();
//		connection.close();
//	}







	@Autowired
	private RabbitTemplate rabbitTemplate;

	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-serial-sensor-terminal2centcontrol";
	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE = "queue-serial-sensor-terminal2centcontrol";
	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_BINDINGKEY = "serial.sensor.terminal2centcontrol";
	private static final String SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINGKEY = "serial.sensor.terminal2centcontrol";

	// 初始化后台相关配置（如队列等）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SERIAL_SENSOR_TERMINAL2CENTCONTROL_QUEUE, declare = "true"),
			exchange = @Exchange(value = SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE, declare = "true", type = SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINETYPE),
			key = SERIAL_SENSOR_TERMINAL2CENTCONTROL_BINDINGKEY
	))

	public void sendSerialSensor(String serialSensorListStr) throws Exception {
		org.springframework.amqp.core.MessageProperties messageProperties = new org.springframework.amqp.core.MessageProperties();
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
		Message message = new Message(serialSensorListStr.getBytes(), messageProperties);

		rabbitTemplate.convertAndSend(SERIAL_SENSOR_TERMINAL2CENTCONTROL_EXCHANGE,
				SERIAL_SENSOR_TERMINAL2CENTCONTROL_ROUTINGKEY,
				message);
	}

	public void sendSerialSensor(List<SerialSensor> serialSensorList) throws Exception {
		this.sendSerialSensor(JsonUtil.obj2String(serialSensorList));
	}
}
