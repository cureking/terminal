package com.renewable.terminal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.renewable.terminal.pojo.AudioAmnout;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author jobob
 * @since 2019-07-08
 */
public interface IAudioAmnoutService extends IService<AudioAmnout> {

	void sendAudioAmnoutList2MQ(List<AudioAmnout> audioAmnoutList);

}
