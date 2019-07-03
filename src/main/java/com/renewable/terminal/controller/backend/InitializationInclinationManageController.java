package com.renewable.terminal.controller.backend;

import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.InitializationInclination;
import com.renewable.terminal.service.IInitializationInclinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @Description：
 * @Author: jarry
 */
@Controller
@RequestMapping("/manage/initialization/inclination/")
public class InitializationInclinationManageController {

	@Autowired
	private IInitializationInclinationService iInitializationInclinationService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse listInitializationInclination() {
		return iInitializationInclinationService.listInitializationInclination();
	}

	@RequestMapping(value = "update.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse updateInitializationInclination(@RequestBody InitializationInclination initializationInclination) {
		return iInitializationInclinationService.updateEnterprise(initializationInclination);
	}

	@RequestMapping(value = "get_by_sensor_register_id.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse getInitializationInclinationBySensorRegisterId(@RequestParam("sensorRegisterId") Integer sensorRegisterId) {
		return iInitializationInclinationService.getInitializationInclinationBySensorRegisterId(sensorRegisterId);
	}
}
