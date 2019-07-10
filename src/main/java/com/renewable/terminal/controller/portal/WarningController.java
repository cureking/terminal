package com.renewable.terminal.controller.portal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.pojo.Warning;
import com.renewable.terminal.service.IAudioDbaService;
import com.renewable.terminal.service.IWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description：
 * @Author: jarry
 */
@RestController
@RequestMapping("/warning/")
public class WarningController {

	@Autowired
	private IWarningService iWarningService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
		Page<Warning> warningPage = new Page<>();
		warningPage.setCurrent(pageNum);
		warningPage.setSize(pageSize);

		IPage<Warning> warningIPage = iWarningService.page(warningPage);
		if (warningIPage == null || warningIPage.getSize() == 0){
			return ServerResponse.createByErrorMessage("warningList is null or warningList's size is zero !");
		}
		return ServerResponse.createBySuccess(warningIPage);
	}

}
