package com.renewable.terminal.service;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SensorRegister;

/**
 * @Description：
 * @Author: jarry
 */
public interface ISensorRegisterService {

	ServerResponse insert(SensorRegister sensorRegister);

	ServerResponse receiveSensorRegisterFromMQ(SensorRegister sensorRegister);

	ServerResponse getSensorRegisterById(Integer sensorRegisterId);

	ServerResponse listSensorRegister();

	ServerResponse update(SensorRegister sensorRegister);

	ServerResponse updateEnterprise(SensorRegister sensorRegister);
}
