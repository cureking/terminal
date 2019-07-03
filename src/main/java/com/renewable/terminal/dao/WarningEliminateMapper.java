package com.renewable.terminal.dao;

import com.renewable.terminal.pojo.WarningEliminate;

public interface WarningEliminateMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(WarningEliminate record);

	int insertSelective(WarningEliminate record);

	WarningEliminate selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(WarningEliminate record);

	int updateByPrimaryKey(WarningEliminate record);
}