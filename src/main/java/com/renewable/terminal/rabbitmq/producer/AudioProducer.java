package com.renewable.terminal.rabbitmq.producer;

import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@Component("AudioProducer")
@Slf4j
public class AudioProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	// 从声音模块开始，简化消息机制（不需要使用复杂的路由规则）
	// 发送传感器数据，全部采用List
	// Audio-Dba
	private static final String AUDIO_DBA_QUEUE = "queue-audio-dba-terminal2centcontrol";
	private static final String AUDIO_DBA_EXCHANGE = "exchange-audio-dba-terminal2centcontrol";
	// Audio-Amnout
	private static final String AUDIO_AMNOUT_QUEUE = "queue-audio-amnout-terminal2centcontrol";
	private static final String AUDIO_AMNOUT_EXCHANGE = "exchange-audio-amnout-terminal2centcontrol";

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(AUDIO_DBA_QUEUE),
			 exchange = @Exchange(AUDIO_DBA_EXCHANGE)
	))
	public void sendAudioDbaList(String audioDbaListStr) {

		log.info("AudioProducer/sendAudioDbaList has sended: {}", audioDbaListStr);

		amqpTemplate.convertAndSend(AUDIO_DBA_QUEUE, audioDbaListStr);
	}

	public void sendAudioDbaList(List<AudioDba> audioDbaList) {
		this.sendAudioDbaList(JsonUtil.obj2String(audioDbaList));
	}

	@RabbitListener(bindings = @QueueBinding(
		value = @Queue(AUDIO_AMNOUT_QUEUE),
		exchange = @Exchange(AUDIO_AMNOUT_EXCHANGE)
	))
	public void sendAudioAmnoutList(String audioAmnoutListStr) {

		log.info("AudioProducer/sendAudioAmnoutList has sended: {}", audioAmnoutListStr);

		amqpTemplate.convertAndSend(AUDIO_AMNOUT_QUEUE, audioAmnoutListStr);
	}

	public void sendAudioAmnoutList(List<AudioAmnout> audioAmnoutList) {
		this.sendAudioAmnoutList(JsonUtil.obj2String(audioAmnoutList));
	}
}
