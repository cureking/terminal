package com.renewable.terminal.controller.portal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.service.IAudioAmnoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description：
 * @Author: jarry
 */
@RestController
@RequestMapping("/audio/amnout/")
public class AudioAmnoutController {

	@Autowired
	private IAudioAmnoutService iAudioAmnoutService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
							   @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		Page<AudioAmnout> audioAmnoutPage = new Page<>();
		audioAmnoutPage.setCurrent(pageNum);
		audioAmnoutPage.setSize(pageSize);

		IPage<AudioAmnout> audioAmnoutIPage = iAudioAmnoutService.page(audioAmnoutPage);
		if (audioAmnoutIPage == null || audioAmnoutIPage.getSize() == 0) {
			return ServerResponse.createByErrorMessage("audioAmnoutList is null or audioAmnoutList's size is zero !");
		}
		return ServerResponse.createBySuccess(audioAmnoutIPage);
	}

	@RequestMapping(value = "get_by_id.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse getById(@RequestParam(value = "id", defaultValue = "1") Integer id) {
		AudioAmnout audioAmnout = iAudioAmnoutService.getById(id);
		if (audioAmnout == null) {
			return ServerResponse.createByErrorMessage("audioAmnout is null with id:" + id);
		}
		return ServerResponse.createBySuccess(audioAmnout);
	}

	@RequestMapping(value = "count.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse count() {
		Integer count = iAudioAmnoutService.count();
		if (count == null) {
			return ServerResponse.createByErrorMessage("count is null !");
		}
		return ServerResponse.createBySuccess(count);
	}
}
