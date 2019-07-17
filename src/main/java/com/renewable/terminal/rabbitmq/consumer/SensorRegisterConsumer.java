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

import java.io.IOException;
import java.util.Map;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Component("SensorRegisterConsumer")
public class SensorRegisterConsumer {

	@Autowired
	private ISensorRegisterService iSensorRegisterService;


	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-sensor-register-centcontrol2terminal";
	private static final String SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE = "queue-sensor-register-centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = SENSOR_REGISTER_CENTCONTROL2TERMINAL_QUEUE),
			exchange = @Exchange(value = SENSOR_REGISTER_CENTCONTROL2TERMINAL_EXCHANGE)
	)
	)
	public void onSensorRegisterMessage(String sensorRegisterStr,
										@Headers Map<String, Object> headers, Channel channel) throws IOException {

		log.info("SensorRegisterConsumer/onSensorRegisterMessage has received: {}", sensorRegisterStr);

		//消费者操作
		SensorRegister sensorRegister = JsonUtil.string2Obj(sensorRegisterStr, SensorRegister.class);
		if (sensorRegister == null || sensorRegister.getTerminalId() == null){
			log.warn("SensorRegisterConsumer/onSensorRegisterMessage has ack sensorRegister. sensorRegister:{}.",sensorRegister.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
			return;
		}

		if (Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)) != sensorRegister.getTerminalId()) {
			log.info("refuse target sensorRegister with terminalId({}).current_terminalId({})", sensorRegister.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			// 临时： 垃圾数据处理
			if (sensorRegister.getTerminalId() == null){
				Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
				channel.basicAck(deliveryTag, false);
				log.warn("ack a gabage sensorRegister :{}!",sensorRegister.toString());
			}
			return;
		}

		ServerResponse response = iSensorRegisterService.receiveSensorRegisterFromMQ(sensorRegister);

		if (response.isSuccess()) {
			log.info("the sensorRegister from centcontrol has consumed . the sensorRegister is {}", sensorRegister.toString());
			//由于配置中写的是手动签收，所以这里需要通过Headers来进行签收
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
