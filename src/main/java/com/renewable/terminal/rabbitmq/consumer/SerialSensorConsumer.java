package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.rabbitmq.pojo.SerialSensorRefresh;
import com.renewable.terminal.service.ISerialSensorService;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;

/**
 * @Description：
 * @Author: jarry
 */
@Component("SerialSensorConsumer")
@Slf4j
public class SerialSensorConsumer {

	@Autowired
	private ISerialSensorService iSerialSensorService;

	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-serial-sensor-centcontrol2terminal";
	private static final String SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE = "queue-serial-sensor-centcontrol2terminal";
	// 终端服务器serialsensor置零操作
	private static final String SERIAL_SENSOR_REFRESH_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-serial-sensor-refresh-centcontrol2terminal";
	private static final String SERIAL_SENSOR_REFRESH_CENTCONTROL2TERMINAL_QUEUE = "queue-serial-sensor-refresh-centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SERIAL_SENSOR_CENTCONTROL2TERMINAL_QUEUE),
			exchange = @Exchange(value = SERIAL_SENSOR_CENTCONTROL2TERMINAL_EXCHANGE)
	))
	public void messageOnSerialSensor(String serialSensorStr,
									  @Headers Map<String, Object> headers, Channel channel) throws IOException {

		log.info("SerialSensorConsumer/messageOnSerialSensor has received: {}", serialSensorStr);

		SerialSensor serialSensor = JsonUtil.string2Obj(serialSensorStr, SerialSensor.class);

		if (Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)) != serialSensor.getTerminalId()) {
			log.info("refuse target serialSensor with terminalId({}).current_terminalId({})", serialSensor.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			return;
		}

		// 2.业务逻辑
		ServerResponse response = iSerialSensorService.receiveSerialSensorFromMQ(serialSensor);

		// 3.确认
		if (response.isSuccess()) {
			// 4.日志记录
			log.info("the serialSensor from centcontrol has consumed . the serialSensor is {}", serialSensor.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}


	// 终端机对应serialsensor置零操作
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SERIAL_SENSOR_REFRESH_CENTCONTROL2TERMINAL_QUEUE),
			exchange = @Exchange(value = SERIAL_SENSOR_REFRESH_CENTCONTROL2TERMINAL_EXCHANGE)
	))
	public void messageOnSerialSensorRefresh(String serialSensorRefreshStr,
											 @Headers Map<String, Object> headers, Channel channel) throws IOException {

		log.info("SerialSensorConsumer/messageOnSerialSensorRefresh has received: {}", serialSensorRefreshStr);

		SerialSensorRefresh serialSensorRefresh = JsonUtil.string2Obj(serialSensorRefreshStr, SerialSensorRefresh.class);

		if (serialSensorRefresh == null || serialSensorRefresh.getTerminalId() == null){
			log.warn("SerialSensorConsumer/messageOnSerialSensorRefresh has ack serialSensorRefresh. serialSensorRefresh:{}.",serialSensorRefresh.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
			return;
		}


		if (Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)) != serialSensorRefresh.getTerminalId()) {
			log.info("refuse target serialSensor with terminalId({}).current_terminalId({})", serialSensorRefresh.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			return;
		}

		// 2.业务逻辑
		ServerResponse response = iSerialSensorService.receiveSerialSensorRefreshFromMQ(serialSensorRefresh);

		// 3.确认
		if (response.isSuccess()) {
			// 4.日志记录
			log.info("the serialSensorRefresh from centcontrol has consumed . the serialSensorRefresh is {}", serialSensorRefresh.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}

}
