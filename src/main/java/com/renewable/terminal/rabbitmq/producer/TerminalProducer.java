package com.renewable.terminal.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.renewable.terminal.pojo.Terminal;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description：
 * @Author: jarry
 */
@Component("TerminalProducer")
public class TerminalProducer {

	private static String rabbitmqHost = "47.92.249.250";
	private static String rabbitmqUser = "admin";
	private static String rabbitmqPassword = "123456";
	private static String rabbitmqPort = "5672";

	private static final String IP_ADDRESS = rabbitmqHost;
	private static final int PORT = Integer.parseInt(rabbitmqPort);
	private static final String USER_NAME = rabbitmqUser;
	private static final String USER_PASSWORD = rabbitmqPassword;

	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-terminal-config-terminal2centcontrol";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE = "queue-terminal-config-terminal2centcontrol";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_BINDINGKEY = "terminal.config.terminal2centcontrol";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINGKEY = "terminal.config.terminal2centcontrol";

	public static void sendTerminalConfig(Terminal terminal) throws IOException, TimeoutException, InterruptedException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername(USER_NAME);
		factory.setPassword(USER_PASSWORD);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE, TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINETYPE, true, false, null);
		channel.queueDeclare(TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE, true, false, false, null);
		channel.queueBind(TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE, TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE, TERMINAL_CONFIG_TERMINAL2CENTCONTROL_BINDINGKEY);

		String terminalStr = JsonUtil.obj2String(terminal);
		channel.basicPublish(TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE, TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, terminalStr.getBytes());

		channel.close();
		connection.close();
	}
}
