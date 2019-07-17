package com.renewable.terminal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.renewable.terminal.dao.AudioAmnoutMapper;
import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.rabbitmq.producer.AudioProducer;
import com.renewable.terminal.service.IAudioAmnoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author jobob
 * @since 2019-07-08
 */
@Service
public class AudioAmnoutServiceImpl extends ServiceImpl<AudioAmnoutMapper, AudioAmnout> implements IAudioAmnoutService {

	@Autowired
	private AudioProducer audioProducer;

	@Override
	public void sendAudioAmnoutList2MQ(List<AudioAmnout> audioAmnoutList) {
		if (audioAmnoutList == null || CollectionUtils.isEmpty(audioAmnoutList)){
			log.warn("the audioAmnoutList is null !");
		}

		// 数据组装（略）

		audioProducer.sendAudioAmnoutList(audioAmnoutList);
	}
}
