package com.renewable.terminal.dao;

import com.renewable.terminal.pojo.SensorRegister;

import java.util.List;

public interface SensorRegisterMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(SensorRegister record);

	int insertSelective(SensorRegister record);

	SensorRegister selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(SensorRegister record);

	int updateByPrimaryKey(SensorRegister record);

	// custom
	int insertOrUpdate(SensorRegister sensorRegister);

	List<SensorRegister> listSensorRegister();
}