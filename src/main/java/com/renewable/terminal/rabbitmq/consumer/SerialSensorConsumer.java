package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.*;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.service.ISerialSensorService;
import com.renewable.terminal.util.JsonUtil;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;

/**
 * @Description：
 * @Author: jarry
 */
@Component("SerialSensorConsumer")
public class SerialSensorConsumer {

//	@Autowired
//	private ISerialSensorService iSerialSensorService;

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
//	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE = "queue-serial-sensor-centcontrol2terminal";
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
//				SerialSensor receiveSerialSensor = JsonUtil.string2Obj(new String(body), SerialSensor.class);
//
//				// 2.验证是否是该终端的消息的消息     // 避免ACK其他终端的消息
//				if (receiveSerialSensor.getTerminalId() == Integer.parseInt(GuavaCache.getKey(TERMINAL_ID))) {
//					// 业务代码
//					ServerResponse response = iSerialSensorService.receiveSerialSensorFromMQ(receiveSerialSensor);
//
//					if (response.isSuccess()) {
//						channel.basicAck(envelope.getDeliveryTag(), false);
//					}
//				}
//			}
//		};
//		channel.basicConsume(SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE, consumer);
//		// 等回调函数执行完毕后，关闭资源
//		// 想了想还是不关闭资源，保持一个监听的状态，从而确保配置的实时更新
////        TimeUnit.SECONDS.sleep(5);
////        channel.close();
////        connection.close();
//	}




	@Autowired
	private ISerialSensorService iSerialSensorService;

	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-serial-sensor-centcontrol2terminal";
	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE = "queue-serial-sensor-centcontrol2terminal";
	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_ROUTINETYPE = "topic";
	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_BINDINGKEY = "serial.sensor.centcontrol2terminal";

	//TODO_FINISHED 2019.05.16 完成终端机TerminalConfig的接收与判断（ID是否为长随机数，是否需要重新分配）
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE, declare = "true"),
			exchange = @Exchange(value = SERIAL_SENSOR_CENTCONTROL2TERMINAL_EXCHANGE, declare = "true", type = SERIAL_SENSOR_CENTCONTROL2TERMINAL_ROUTINETYPE),
			key = SERIAL_SENSOR_CENTCONTROL2TERMINAL_BINDINGKEY
	))
	@RabbitHandler
	public void messageOnSerialSensor(@Payload String serialSensorStr, @Headers Map<String, Object> headers, Channel channel) throws IOException {

		SerialSensor serialSensor = JsonUtil.string2Obj(serialSensorStr, SerialSensor.class);

		// 2.业务逻辑
		ServerResponse response = iSerialSensorService.receiveSerialSensorFromMQ(serialSensor);

		// 3.确认
		if (response.isSuccess()) {
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
