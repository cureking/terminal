package com.renewable.terminal.controller.portal;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.service.ISerialSensorService;
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
@RequestMapping("/sensor/serial/")
public class SerialSensorController {

	@Autowired
	private ISerialSensorService iSerialSensorService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse listSerialSensor() {
		return iSerialSensorService.list();
	}
}
