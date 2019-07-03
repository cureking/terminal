package com.renewable.terminal.controller.portal;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.service.ITerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description：
 * @Author: jarry
 */
@Controller
@RequestMapping("/terminal/")
public class TerminalController {

	@Autowired
	private ITerminalService iTerminalService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse listTerminal() {
		return iTerminalService.listTerminal();
	}
}
