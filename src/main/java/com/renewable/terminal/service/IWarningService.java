package com.renewable.terminal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Warning;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
public interface IWarningService extends IService<Warning> {

	ServerResponse insertWarning(Warning warning);

	ServerResponse insertWarningList(List<Warning> warningList);

	ServerResponse stateCheck();

	ServerResponse inclinationInitCheck();
}
