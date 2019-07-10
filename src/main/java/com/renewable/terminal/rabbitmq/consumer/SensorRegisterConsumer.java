package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SensorRegister;
import com.renewable.terminal.service.ISensorRegisterService;
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
@Component("SensorRegisterConsumer")
public class SensorRegisterConsumer {
//
//	@Autowired
//	private ISensorRegisterService iSensorRegisterService;

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
//	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE = "queue-sensor-register-centcontrol2terminal";
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
//				SensorRegister receiveSensorRegister = JsonUtil.string2Obj(new String(body), SensorRegister.class);
//
//				// 2.由于一些UNACK的原因，这里进行空值判断，后面消除
//				if (receiveSensorRegister == null){
//					channel.basicAck(envelope.getDeliveryTag(), false);
//				}
//
//				// 2.验证是否是该终端的消息的消息     // 避免ACK其他终端的消息
//				if (receiveSensorRegister.getTerminalId() == Integer.parseInt(GuavaCache.getKey(TERMINAL_ID))) {
//					// 业务代码
//					ServerResponse response = iSensorRegisterService.receiveSensorRegisterFromMQ(receiveSensorRegister);
//
//					if (response.isSuccess()) {
//						channel.basicAck(envelope.getDeliveryTag(), false);
//					}
//				}
//			}
//		};
//		channel.basicConsume(SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE, consumer);
//		// 等回调函数执行完毕后，关闭资源
//		// 想了想还是不关闭资源，保持一个监听的状态，从而确保配置的实时更新
////        TimeUnit.SECONDS.sleep(5);
////        channel.close();
////        connection.close();
//	}


	@Autowired
	private ISensorRegisterService iSensorRegisterService;


	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-sensor-register-centcontrol2terminal";
	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE = "queue-sensor-register-centcontrol2terminal";
	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_ROUTINETYPE = "topic";
	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_BINDINGKEY = "sensor.register.centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE, declare = "true"),
			exchange = @Exchange(value = SENSOR_REGISTER_CENTCONTROL2TERMINAL_EXCHANGE, durable = "true", type = SENSOR_REGISTER_CENTCONTROL2TERMINAL_ROUTINETYPE),
			key = SENSOR_REGISTER_CENTCONTROL2TERMINAL_BINDINGKEY
	)
	)
	@RabbitHandler
	public void onSensorRegisterMessage(@Payload String sensorRegisterStr,
										@Headers Map<String, Object> headers,
										Channel channel) throws Exception {
		//消费者操作
		SensorRegister sensorRegister = JsonUtil.string2Obj(sensorRegisterStr, SensorRegister.class);

		if (!GuavaCache.getKey(TERMINAL_ID).equals(sensorRegister.getTerminalId())) {
			log.info("refuse target sensorRegister with terminalId({}).current_terminalId({})", sensorRegister.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			return;
		}

		ServerResponse response = iSensorRegisterService.receiveSensorRegisterFromMQ(sensorRegister);

		if (response.isSuccess()) {
			//由于配置中写的是手动签收，所以这里需要通过Headers来进行签收
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
