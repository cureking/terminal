package com.renewable.terminal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.renewable.terminal.dao.AudioDbaMapper;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.rabbitmq.producer.AudioProducer;
import com.renewable.terminal.service.IAudioDbaService;
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
public class AudioDbaServiceImpl extends ServiceImpl<AudioDbaMapper, AudioDba> implements IAudioDbaService {

	@Autowired
	private AudioProducer audioProducer;

	@Override
	public void sendAudioAmnoutList2MQ(List<AudioDba> audioDbaList) {
		if (audioDbaList == null || CollectionUtils.isEmpty(audioDbaList)){
			log.warn("the audioDbaList is null !");
		}

		// 数据组装（略）

		audioProducer.sendAudioDbaList(audioDbaList);
	}
}
