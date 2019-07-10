package com.renewable.terminal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewable.terminal.pojo.Warning;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WarningMapper  extends BaseMapper<Warning> {
	int deleteByPrimaryKey(Integer id);

	int insert(Warning record);

	int insertSelective(Warning record);

	Warning selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(Warning record);

	int updateByPrimaryKey(Warning record);

	// custom
	Long selectLastOringinId();

	int insertBatch(@Param("warningList") List<Warning> warningList);
}