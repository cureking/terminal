package com.renewable.terminal.service;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.rabbitmq.pojo.SerialSensorRefresh;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
public interface ISerialSensorService {

	ServerResponse insert(SerialSensor serialSensor);

	ServerResponse insertBatch(List<SerialSensor> serialSensorList);

	ServerResponse update(SerialSensor serialSensor);

	ServerResponse updateByUser(SerialSensor serialSensor);

	ServerResponse getSerialSensorByPortAndAddress(String port, String address);

	ServerResponse getSerialSensorBySensorRegisterId(Integer sensorRegisterId);

	ServerResponse list();

	ServerResponse refresh();

	ServerResponse sendSerialSensor2MQ(List<SerialSensor> serialSensorList);

	ServerResponse receiveSerialSensorFromMQ(SerialSensor serialSensor);

	ServerResponse receiveSerialSensorRefreshFromMQ(SerialSensorRefresh serialSensorRefresh);

	// 定时任务-读取终端数据
	ServerResponse taskLoadFromSerialSensor();

	ServerResponse loadFromSerialSensorByList(List<SerialSensor> serialSensorList);

	/**
	 * 串口设备置零
	 *
	 * @param sensorRegisterId
	 * @param type             00表示绝对零点，01表示相对零点
	 * @return
	 */
	ServerResponse zeroReset(Integer sensorRegisterId, String type);

}
