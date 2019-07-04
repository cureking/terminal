package com.renewable.terminal.rabbitmq.consumer;

import com.rabbitmq.client.*;
import com.renewable.terminal.Init.SerialSensorInit;
import com.renewable.terminal.Init.TerminalInit;
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
@Slf4j
public class TerminalConsumer {

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
		if (terminal == null){
			log.info("consume the null terminal config !");
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
		if (!GuavaCache.getKey(TERMINAL_MAC).equals(terminal.getMac())){
			log.info("refuse target terminal with mac({}) configure to this terminal with mac({}).",terminal.getMac(), GuavaCache.getKey(TERMINAL_MAC));
			return;
		}

		// 2.业务逻辑
		ServerResponse response = iTerminalService.receiveTerminalFromRabbitmq(terminal);

		// 3.temp 临时做的逻辑（由于原有的INIT存在单线程不释放资源的问题。所以将原来serialInit的操作放这里		// 当然也有可能之后放在上面的服务中，毕竟刷新终端信息后，继续刷新传感器信息也是有道理的嘛。
		log.info("start serialSensorInit");
		serialSensorInit.init();

		// 3.确认
		if (response.isSuccess()) {
			Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
			channel.basicAck(deliveryTag, false);
		}
	}
}
