package com.renewable.terminal.dao;

import com.renewable.terminal.pojo.SerialSensor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SerialSensorMapper {
	int deleteByPrimaryKey(Short id);

	int insert(SerialSensor record);

	int insertSelective(SerialSensor record);

	SerialSensor selectByPrimaryKey(Short id);

	int updateByPrimaryKeySelective(SerialSensor record);

	int updateByPrimaryKey(SerialSensor record);

	// custom
	int insertBatch(@Param("serialSensorList") List<SerialSensor> serialSensorList);

	List<SerialSensor> selectByTerminalIdAndPort(@Param("terminalId") int terminalId, @Param("port") String port);

	SerialSensor selectByTerminalIdAndPortAndAddress(@Param("terminalId") int terminalId, @Param("port") String port, @Param("address") String address);

	List<SerialSensor> listSeriaLSensor();

	int insertOrUpdate(SerialSensor serialSensor);

	SerialSensor selectBySensorRegisterId(Integer sensorRegisterId);
}