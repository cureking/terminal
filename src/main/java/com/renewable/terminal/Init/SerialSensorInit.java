package com.renewable.terminal.Init;

import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.common.constant.CacheConstant;
import com.renewable.terminal.service.ISerialSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description：
 * @Author: jarry
 */
@Component
@Slf4j
public class SerialSensorInit {

	@Autowired
	private ISerialSensorService iSerialSensorService;

	public ServerResponse init() {

		log.info("start NO.{} terminal serialSensorInit",
				GuavaCache.getKey(CacheConstant.TERMINAL_ID));

		ServerResponse serialSensorInitResponse = iSerialSensorService.refresh();     // 这里的serialSensor初始化，会带动关联的sensorRegister与initializationInclination等初始化。   （终于理清楚这里面应有的逻辑顺序了，逻辑实体建立于物理实体上）
		if (serialSensorInitResponse.isFail()) {
			return serialSensorInitResponse;
		}

		System.out.println("SerialSensorInit end");
		return ServerResponse.createBySuccessMessage("SerialSensorInit end");

	}

}
