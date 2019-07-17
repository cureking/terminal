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

import java.io.IOException;
import java.util.Map;

import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Component("InitializationInclinationConsumer")
public class InitializationInclinationConsumer {

	@Autowired
	private IInitializationInclinationService iInitializationInclinationService;


	//IP USERNAME PASSWORD等都是自动注入的

	//目前业务规模还很小，没必要设置太复杂的命名规则与路由规则。不过，可以先保留topic的路由策略，便于日后扩展。
	// Initialization_Inclination 相关配置
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE = "exchange-initialization-inclination-centcontrol2terminal";
	private static final String INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE = "queue-initialization-inclination-centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(     // 要设置到底监听哪个QUEUE    还可以进行EXCHANGE,QUEUE,BINGDING
			value = @Queue(value = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_QUEUE),
			exchange = @Exchange(value = INITIALIZATION_INCLINATION_TERMINAL2CENTCONTROL_EXCHANGE)
	)
	)
	public void onInitializationInclinationMessage(String initializationInclinationStr,
												   @Headers Map<String, Object> headers, Channel channel) throws IOException {

		log.info("InitializationInclinationConsumer/onInitializationInclinationMessage has received: {}", initializationInclinationStr);

		// 1.接收数据，并反序列化出对象
		InitializationInclination initializationInclination = JsonUtil.string2Obj(initializationInclinationStr, InitializationInclination.class);

		if (initializationInclination == null || initializationInclination.getTerminalId() == null) {
			log.warn("InitializationInclinationConsumer/onInitializationInclinationMessage has ack initializationInclination. initializationInclination:{}.",initializationInclination.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
			return;
		}

		if (Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)) != initializationInclination.getTerminalId()) {
			log.info("refuse target initializationInclination with terminalId({}).current_terminalId({})", initializationInclination.getTerminalId(), GuavaCache.getKey(TERMINAL_ID));
			return;
		}

		ServerResponse response = iInitializationInclinationService.receiveInitializationInclinationFromMQ(initializationInclination);

		if (response.isSuccess()) {
			//由于配置中写的是手动签收，所以这里需要通过Headers来进行签收
			log.info("the initializationInclination from centcontrol has consumed . the initializationInclination is {}", initializationInclination.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}


}
