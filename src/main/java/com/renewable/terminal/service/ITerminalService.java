package com.renewable.terminal.service;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Terminal;

/**
 * @Description：
 * @Author: jarry
 */
public interface ITerminalService {

	ServerResponse listTerminal();

	ServerResponse insertTerminal(Terminal terminal);

	ServerResponse receiveTerminalFromRabbitmq(Terminal terminal);

	ServerResponse refreshCacheFromDB();    // 目测这个没有过高的价值，可以先暂时放着，之后进行清理

	ServerResponse refreshConfigFromCent();
}
