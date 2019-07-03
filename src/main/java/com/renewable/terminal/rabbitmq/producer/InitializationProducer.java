package com.renewable.terminal.rabbitmq.producer;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.renewable.terminal.pojo.InitializationInclination;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author: jarry
 */
@Component("InitializationProducer")
public class InitializationProducer {

	private static String rabbitmqHost = "47.92.249.250";
	private static String rabbitmqUser = "admin";
	private static String rabbitmqPassword = "123456";
	private static String rabbitmqPort = "5672";

	private static final String IP_ADDRESS = rabbitmqHost;
	private static final int PORT = Integer.parseInt(rabbitmqPort);
	private static final String USER_NAME = rabbitmqUser;
	private static final String USER_PASSWORD = rabbitmqPassword;

	// 目前业务规模还很小，没必要设置太复杂的命名规则与路由规则。不过，可以先保留topic的路由策略，便于日后扩展。
	// inclinationTotal 相关配置
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-initialization-inclination-centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE = "queue-initialization-inclination-centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_BINDINGKEY = "sensor.initialization.inclination.centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINGKEY = "sensor.initialization.inclination.centcontrol2terminal";

	public static void sendInitializationInclination(List<InitializationInclination> initializationInclinationList) throws IOException, TimeoutException, InterruptedException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername(USER_NAME);
		factory.setPassword(USER_PASSWORD);

		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();
		channel.exchangeDeclare(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE, INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINETYPE, true, false, null);      // String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments
		channel.queueDeclare(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE, true, false, false, null);               // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
		channel.queueBind(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE, INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE, INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_BINDINGKEY);

		String initializationInclinationListStr = JsonUtil.obj2StringPretty(initializationInclinationList);
		channel.basicPublish(INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE, INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, initializationInclinationListStr.getBytes());

		channel.close();
		connection.close();
	}

}
