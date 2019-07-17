package com.renewable.terminal.controller.portal;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.service.IAudioDbaService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@RestController
@RequestMapping("/audio/dba/")
public class AudioDbaController{

	@Autowired
	private IAudioDbaService iAudioDbaService;

	@RequestMapping(value = "list.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
		Page<AudioDba> audioDbaPage = new Page<>();
		audioDbaPage.setCurrent(pageNum);
		audioDbaPage.setSize(pageSize);

		IPage<AudioDba> audioDbaIPage = iAudioDbaService.page(audioDbaPage);
		if (audioDbaIPage == null || audioDbaIPage.getSize() == 0){
			return ServerResponse.createByErrorMessage("audioDbaList is null or audioDbaList's size is zero !");
		}
		return ServerResponse.createBySuccess(audioDbaIPage);
	}

	@RequestMapping(value = "list_time.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse listByTime(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
									 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
									 @RequestParam(value = "startTime", defaultValue = "1970-1-1 0:0:0") String startTime,
									 @RequestParam(value = "endTime", defaultValue = "2100-03-14 11:33:54") String endTime) {
		Page<AudioDba> audioDbaPage = new Page<>();
		audioDbaPage.setCurrent(pageNum);
		audioDbaPage.setSize(pageSize);

		Wrapper<AudioDba> audioDbaWrapper = new QueryWrapper<AudioDba>().between("create_time",startTime,endTime);

		IPage<AudioDba> audioDbaIPage = iAudioDbaService.page(audioDbaPage,audioDbaWrapper );
		if (audioDbaIPage == null || audioDbaIPage.getSize() == 0) {
			return ServerResponse.createByErrorMessage("audioDbaList is null or audioDbaList's size is zero !");
		}
		return ServerResponse.createBySuccess(audioDbaIPage);
	}

	@RequestMapping(value = "get_by_id.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse getById(@RequestParam(value = "id", defaultValue = "1")Integer id){
		AudioDba audioDba = iAudioDbaService.getById(id);
		if (audioDba == null){
			return ServerResponse.createByErrorMessage("audioDba is null with id:" + id);
		}
		return ServerResponse.createBySuccess(audioDba);
	}

	@RequestMapping(value = "count.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse count(){
		Integer count = iAudioDbaService.count();
		if (count == null){
			return ServerResponse.createByErrorMessage("count is null !");
		}
		return ServerResponse.createBySuccess(count);
	}
}
