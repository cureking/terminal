package com.renewable.terminal.dao;

import com.renewable.terminal.pojo.InclinationDealedTotal;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface InclinationDealedTotalMapper {
	int deleteByPrimaryKey(Long id);

	int insert(InclinationDealedTotal record);

	int insertSelective(InclinationDealedTotal record);

	InclinationDealedTotal selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(InclinationDealedTotal record);

	int updateByPrimaryKey(InclinationDealedTotal record);

	//custom
	List<InclinationDealedTotal> selectList(int sensor_identifier);

	List<InclinationDealedTotal> selectListByVersionAndLimit(@Param("version") String version, @Param("limit") int limit);

	List<InclinationDealedTotal> selectListByTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("sensor_identifier") int sensor_identifier);

	int updateVersionBatch(@Param(value = "inclinationDealedTotalList") List<InclinationDealedTotal> inclinationDealedTotalList);
}