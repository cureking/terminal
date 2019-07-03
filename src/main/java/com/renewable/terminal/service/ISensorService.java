package com.renewable.terminal.service;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Sensor;

/**
 * @Description：
 * @Author: jarry
 */
public interface ISensorService {

	ServerResponse insertSensor(Sensor sensor);

	ServerResponse updateSensor(Sensor sensor);

	ServerResponse getSensorById(Integer sensorId);

	ServerResponse listSensor();

}
