package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.renewable.terminal.Init.SerialSensorInit;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Terminal;
import com.renewable.terminal.service.ITerminalService;
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
import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_MAC;

/**
 * @Description：
 * @Author: jarry
 */
@Component
@Slf4j
public class TerminalConsumer {

	@Autowired
	private ITerminalService iTerminalService;

	@Autowired
	private SerialSensorInit serialSensorInit;


	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_EXCHANGE = "exchange-terminal-config-centcontrol2terminal";
	private static final String TERMINAL_CONFIG_CENTCONTROL2TERMINAL_QUEUE = "queue-terminal-config-centcontrol2terminal";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = TERMINAL_CONFIG_CENTCONTROL2TERMINAL_QUEUE),
			exchange = @Exchange(value = TERMINAL_CONFIG_CENTCONTROL2TERMINAL_EXCHANGE)
	))
	public void messageOnTerminal(String terminalStr,
								  @Headers Map<String, Object> headers, Channel channel) throws IOException {

		log.info("TerminalConsumer/messageOnTerminal has received: {}", terminalStr);

		Terminal terminal = JsonUtil.string2Obj(terminalStr, Terminal.class);
//		if (terminal == null || terminal.getMac() == null) {
//			log.info("consume the null terminal config !");
//			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
//			channel.basicAck(deliveryTag, false);
//		}

		if (terminal == null){
			log.warn("TerminalConsumer/messageOnTerminal has ack terminal. terminal:{}.",terminal.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
			return;
		}

		if (Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)) != terminal.getId()) {
			if (!GuavaCache.getKey(TERMINAL_MAC).equals(terminal.getMac())){
				log.info("refuse target terminal with id({}) and mac({}) configure to this terminal with id({}) and mac({}).", terminal.getId(), terminal.getMac(), GuavaCache.getKey(TERMINAL_ID), GuavaCache.getKey(TERMINAL_MAC));
				return;
			}
		}

		// 2.业务逻辑
		ServerResponse response = iTerminalService.receiveTerminalFromRabbitmq(terminal);

		// 3.temp 临时做的逻辑（由于原有的INIT存在单线程不释放资源的问题。所以将原来serialInit的操作放这里		// 当然也有可能之后放在上面的服务中，毕竟刷新终端信息后，继续刷新传感器信息也是有道理的嘛。
		log.info("start serialSensorInit");
		serialSensorInit.init();

		// 3.确认
		if (response.isSuccess()) {
			// 4.日志记录
			log.info("the terminal from centcontrol has consumed . the terminal is {}", terminal.toString());
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
