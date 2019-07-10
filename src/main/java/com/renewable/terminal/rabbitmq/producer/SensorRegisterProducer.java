package com.renewable.terminal.rabbitmq.producer;


import com.renewable.terminal.pojo.SensorRegister;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.amqp.core.Message;
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
@Component("SensorRegisterProducer")
public class SensorRegisterProducer {
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

	// 目前业务规模还很小，没必要设置太复杂的命名规则与路由规则。不过，可以先保留topic的路由策略，便于日后扩展。
	// sensorRegister 相关配置
//	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE = "sensor-register-exchange-terminal2centcontrol";
//	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE = "sensor-register-queue-terminal2centcontrol";
//	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
//	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_BINDINGKEY = "sensor.register.config.terminal2centcontrol";
//	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINGKEY = "sensor.register.config.terminal2centcontrol";

//	public static void sendSensorRegister(List<SensorRegister> sensorRegisterList) throws IOException, TimeoutException, InterruptedException {
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setHost(IP_ADDRESS);
//		factory.setPort(PORT);
//		factory.setUsername(USER_NAME);
//		factory.setPassword(USER_PASSWORD);
//
//		Connection connection = factory.newConnection();
//
//		Channel channel = connection.createChannel();
//		channel.exchangeDeclare(SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE, SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINETYPE, true, false, null);      // String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments
//		channel.queueDeclare(SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE, true, false, false, null);               // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
//		channel.queueBind(SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE, SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE, SENSOR_REGISTER_TERMINAL2CENTCONTROL_BINDINGKEY);
//
//		String sensorRegisterListStr = JsonUtil.obj2StringPretty(sensorRegisterList);
//		channel.basicPublish(SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE, SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, sensorRegisterListStr.getBytes());
//
//		channel.close();
//		connection.close();
//	}


	@Autowired
	private RabbitTemplate rabbitTemplate;

	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-sensor-register-terminal2centcontrol";
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE = "queue-sensor-register-terminal2centcontrol";
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_BINDINGKEY = "sensor.register.config.terminal2centcontrol";
	private static final String SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINGKEY = "sensor.register.config.terminal2centcontrol";

	// 初始化后台相关配置（如队列等）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SENSOR_REGISTER_TERMINAL2CENTCONTROL_QUEUE, declare = "true"),
			exchange = @Exchange(value = SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE, declare = "true", type = SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINETYPE),
			key = SENSOR_REGISTER_TERMINAL2CENTCONTROL_BINDINGKEY
	))

	public void sendSensorRegister(String sensorRegisterListStr) throws Exception {

		org.springframework.amqp.core.MessageProperties messageProperties = new org.springframework.amqp.core.MessageProperties();
		messageProperties.setContentType(org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
		Message message = new Message(sensorRegisterListStr.getBytes(), messageProperties);

		rabbitTemplate.convertAndSend(SENSOR_REGISTER_TERMINAL2CENTCONTROL_EXCHANGE,
				SENSOR_REGISTER_TERMINAL2CENTCONTROL_ROUTINGKEY,
				message);
	}

	public void sendSensorRegister(List<SensorRegister> sensorRegisterList) throws Exception {
		this.sendSensorRegister(JsonUtil.obj2StringPretty(sensorRegisterList));
	}

}
