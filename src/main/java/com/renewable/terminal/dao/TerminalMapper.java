package com.renewable.terminal.dao;

import com.renewable.terminal.pojo.Terminal;

import java.util.List;

public interface TerminalMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(Terminal record);

	int insertSelective(Terminal record);

	Terminal selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(Terminal record);

	int updateByPrimaryKey(Terminal record);

	//custom
	List<Terminal> listTerminal();

	int updateByMac(Terminal terminal);
}