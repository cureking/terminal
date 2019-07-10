package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.InitializationInclination;
import com.renewable.terminal.service.IInitializationInclinationService;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Component("InitializationInclinationConsumer")
public class InitializationInclinationConsumer {

//
//	@Autowired
//	private IInitializationInclinationService iInitializationInclinationService;

//	@Value("$RABBITMQ_HOST")
//	private String RABBITMQ_HOST;
//	@Value("RABBITMQ_USER_NAME")
//	private String RABBITMQ_USER_NAME;
//	@Value("RABBITMQ_USER_PASSWORD")
//	private String RABBITMQ_USER_PASSWORD;
//
//	private String rabbitmqHost = "47.92.249.250";
//	private String rabbitmqUser = "admin";
//	private String rabbitmqPassword = "123456";
//
//	private static final String INITIALIZATION_INCLINATION__CENTCONTROL2TERMINAL_QUEUE = "queue-initialization-inclination-centcontrol2terminal";
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
//				InitializationInclination receiveInitializationInclination = JsonUtil.string2Obj(new String(body), InitializationInclination.class);
//
//				// 2.由于一些UNACK的原因，这里进行空值判断，后面消除
//				if (receiveInitializationInclination == null){
//					channel.basicAck(envelope.getDeliveryTag(), false);
//				}
//
//				// 2.验证是否是该终端的消息的消息     // 避免ACK其他终端的消息
//				if (receiveInitializationInclination.getTerminalId() == Integer.parseInt(GuavaCache.getKey(TERMINAL_ID))) {
//					// 业务代码
//					ServerResponse response = iInitializationInclinationService.receiveInitializationInclinationFromMQ(receiveInitializationInclination);
//
//					if (response.isSuccess()) {
//						channel.basicAck(envelope.getDeliveryTag(), false);
//					}
//				}
//			}
//		};
//		channel.basicConsume(INITIALIZATION_INCLINATION__CENTCONTROL2TERMINAL_QUEUE, consumer);
//	}


	@Autowired
	private IInitializationInclinationService iInitializationInclinationService;


	//IP USERNAME PASSWORD等都是自动注入的

	//目前业务规模还很小，没必要设置太复杂的命名规则与路由规则。不过，可以先保留topic的路由策略，便于日后扩展。
	// Initialization_Inclination 相关配置
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-initialization-inclination-centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE = "queue-initialization-inclination-centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINETYPE = "topic";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_BINDINGKEY = "sensor.initialization.inclination.centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(     // 要设置到底监听哪个QUEUE    还可以进行EXCHANGE,QUEUE,BINGDING
			value = @Queue(value = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE, declare = "true"),
			exchange = @Exchange(value = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE, durable = "true", type = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_ROUTINETYPE),
			key = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_BINDINGKEY
	)
	)
	@RabbitHandler
	public void onInitializationInclinationMessage(@Payload String initializationInclinationStr,
												   @Headers Map<String, Object> headers,
												   Channel channel) throws Exception {
		// 1.接收数据，并反序列化出对象
		InitializationInclination initializationInclination = JsonUtil.string2Obj(new String(initializationInclinationStr), InitializationInclination.class);

		if (initializationInclination == null) {
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}

		if (!GuavaCache.getKey(TERMINAL_ID).equals(initializationInclination.getTerminalId())) {
			log.info("refuse target initializationInclination with terminalId({}).current_terminalId({})", initializationInclination.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			return;
		}

		ServerResponse response = iInitializationInclinationService.receiveInitializationInclinationFromMQ(initializationInclination);

		if (response.isSuccess()) {
			//由于配置中写的是手动签收，所以这里需要通过Headers来进行签收
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}


}
