package com.renewable.terminal.controller.backend;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.Terminal;
import com.renewable.terminal.service.ITerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description：
 * @Author: jarry
 */
@Controller
@RequestMapping("/manage/terminal/")
public class TerminalManageController {

	@Autowired
	private ITerminalService iTerminalService;

	@RequestMapping(value = "update_name.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse updateName(@RequestBody Terminal terminal) {
		return iTerminalService.updateName(terminal);
	}
}
