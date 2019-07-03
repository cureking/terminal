package com.renewable.terminal.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.renewable.terminal.pojo.Warning;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author: jarry
 */
@Component("WarningProducer")
public class WarningProducer {

	private static String rabbitmqHost = "47.92.249.250";
	private static String rabbitmqUser = "admin";
	private static String rabbitmqPassword = "123456";
	private static String rabbitmqPort = "5672";

	private static final String IP_ADDRESS = rabbitmqHost;
	private static final int PORT = Integer.parseInt(rabbitmqPort);
	private static final String USER_NAME = rabbitmqUser;
	private static final String USER_PASSWORD = rabbitmqPassword;

	// 目前业务规模还很小，没必要设置太复杂的命名规则与路由规则。不过，可以先保留topic的路由策略，便于日后扩展。
	// Warning 相关配置     // 这边警报的路由规则还是考虑一下吧，毕竟警报一定是订阅模型的，分发给多个消费者类型。
	private static final String WARNING_TERMINAL2CENTCONTROL_EXCHANGE = "warning-exchange-terminal2centcontrol";
	private static final String WARNING_TERMINAL2CENTCONTROL_QUEUE = "warning-inclination-queue-terminal2centcontrol";
	private static final String WARNING_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String WARNING_TERMINAL2CENTCONTROL_BINDINGKEY = "warning.inclination.terminal2centcontrol";
	private static final String WARNING_TERMINAL2CENTCONTROL_ROUTINGKEY = "warning.inclination.terminal2centcontrol";


	public static void sendWarning(List<Warning> inclinationTotalList) throws IOException, TimeoutException, InterruptedException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername(USER_NAME);
		factory.setPassword(USER_PASSWORD);

		Connection connection = factory.newConnection();

		Channel channel = connection.createChannel();
		channel.exchangeDeclare(WARNING_TERMINAL2CENTCONTROL_EXCHANGE, WARNING_TERMINAL2CENTCONTROL_ROUTINETYPE, true, false, null);      // String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments
		channel.queueDeclare(WARNING_TERMINAL2CENTCONTROL_QUEUE, true, false, false, null);               // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
		channel.queueBind(WARNING_TERMINAL2CENTCONTROL_QUEUE, WARNING_TERMINAL2CENTCONTROL_EXCHANGE, WARNING_TERMINAL2CENTCONTROL_BINDINGKEY);

		String inclinationTotalListStr = JsonUtil.obj2StringPretty(inclinationTotalList);
		channel.basicPublish(WARNING_TERMINAL2CENTCONTROL_EXCHANGE, WARNING_TERMINAL2CENTCONTROL_ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, inclinationTotalListStr.getBytes());

		channel.close();
		connection.close();
	}
}
