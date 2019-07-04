package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.*;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.service.ISerialSensorService;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SerialSensorConsumer {

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

		// 4.日志记录
		log.info("the serialSensor from centcontrol has consumed . the serialSensor is {}", serialSensorStr);
	}
}
