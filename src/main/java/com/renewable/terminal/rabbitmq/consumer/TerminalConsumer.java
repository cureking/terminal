package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.*;
import com.renewable.terminal.Init.SerialSensorInit;
import com.renewable.terminal.Init.TerminalInit;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Terminal;
import com.renewable.terminal.service.ITerminalService;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;
import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_MAC;

/**
 * @Description：
 * @Author: jarry
 */
@Component
public class TerminalConsumer {

//	@Autowired
//	private ITerminalService iTerminalService;

	//	@Value("$RABBITMQ_HOST")
//	private String RABBITMQ_HOST;
//	@Value("RABBITMQ_USER_NAME")
//	private String RABBITMQ_USER_NAME;
//	@Value("RABBITMQ_USER_PASSWORD")
//	private String RABBITMQ_USER_PASSWORD;

//	private String rabbitmqHost = "47.92.249.250";
//	private String rabbitmqUser = "admin";
//	private String rabbitmqPassword = "123456";
//
//	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-terminal-config-centcontrol2terminal";
//	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_QUEUE = "queue-terminal-config-centcontrol2terminal";
//	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_ROUTINETYPE = "topic";
//	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_BINDINGKEY = "terminal.config.centcontrol2terminal";
//
//	@PostConstruct
//	public void messageOnTerminal() throws IOException, TimeoutException, InterruptedException {
//		Address[] addresses = new Address[]{
//				new Address(rabbitmqHost)
//		};
//		ConnectionFactory factory = new ConnectionFactory();
//		factory.setUsername(rabbitmqUser);
//		factory.setPassword(rabbitmqPassword);
//
//		Connection connection = factory.newConnection(addresses);
//		final Channel channel = connection.createChannel();
//		channel.basicQos(64);   // 设置客户端最多接收未ack的消息个数，避免客户端被冲垮（常用于限流）
//		Consumer consumer = new DefaultConsumer(channel) {
//
//
//			@Override
//			public void handleDelivery(String consumerTag,
//									   Envelope envelope,
//									   AMQP.BasicProperties properties,
//									   byte[] body) throws IOException {
//				// 1.接收数据，并反序列化出对象
//				Terminal receiveTerminalConfig = JsonUtil.string2Obj(new String(body), Terminal.class);
//
//
//
//				// 2.验证是否是该终端的消息的消息     // 避免ACK其他终端的消息
//				if (GuavaCache.getKey(TERMINAL_MAC).equals(receiveTerminalConfig.getMac()) || receiveTerminalConfig.getId().toString().equals(GuavaCache.getKey(TERMINAL_ID))) {
//					// 业务代码
//					ServerResponse response = iTerminalService.receiveTerminalFromRabbitmq(receiveTerminalConfig);
//					// 重启之后，就解决问题了。 果然是重启大法好啊。  // 然后又出问题了。。。不过目前找到问题的根源是因为双方content-type设置的问题  // 终于解决了。确实是上述问题造成的。不过在那儿之后又引出了@Payload的问题，不过也解决了。但是对原理还是不够了解，只是从应用角度解决（碰巧，这种解决方案不算太难看）
//
//					if (response.isSuccess()) {
//						channel.basicAck(envelope.getDeliveryTag(), false);
//					}
//				}
//			}
//		};
//		channel.basicConsume(TERMINAL_CONFIG_CENTCONTROL2TERMINAL_QUEUE, consumer);
//		// 等回调函数执行完毕后，关闭资源
//		// 想了想还是不关闭资源，保持一个监听的状态，从而确保配置的实时更新
////        TimeUnit.SECONDS.sleep(5);
////        channel.close();
////        connection.close();
//	}






	@Autowired
	private ITerminalService iTerminalService;

	@Autowired
	private SerialSensorInit serialSensorInit;


	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-terminal-config-centcontrol2terminal";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE = "queue-terminal-config-centcontrol2terminal";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String TERMINAL_CONFIG_TERMINAL2CENTCONTROL_BINDINGKEY = "terminal.config.centcontrol2terminal";

	//TODO_FINISHED 2019.05.16 完成终端机TerminalConfig的接收与判断（ID是否为长随机数，是否需要重新分配）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_QUEUE, declare = "true"),
			exchange = @Exchange(value = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_EXCHANGE, declare = "true", type = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_ROUTINETYPE),
			key = TERMINAL_CONFIG_TERMINAL2CENTCONTROL_BINDINGKEY
	))
	@RabbitHandler
	public void messageOnTerminal(@Payload String terminalStr, @Headers Map<String, Object> headers, Channel channel) throws IOException {

		Terminal terminal = JsonUtil.string2Obj(terminalStr, Terminal.class);

		// 2.业务逻辑
		ServerResponse response = iTerminalService.receiveTerminalFromRabbitmq(terminal);

		// 3.temp 临时做的逻辑（由于原有的INIT存在单线程不释放资源的问题。所以将原来serialInit的操作放这里		// 当然也有可能之后放在上面的服务中，毕竟刷新终端信息后，继续刷新传感器信息也是有道理的嘛。
		serialSensorInit.init();

		// 3.确认
		if (response.isSuccess()) {
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
