package com.renewable.terminal.service;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Warning;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
public interface IWarningService {

	ServerResponse insertWarning(Warning warning);

	ServerResponse insertWarningList(List<Warning> warningList);

	ServerResponse stateCheck();

	ServerResponse inclinationInitCheck();
}
