package com.renewable.terminal.service.impl;

import com.renewable.terminal.common.Const;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.dao.TerminalMapper;
import com.renewable.terminal.pojo.Terminal;
import com.renewable.terminal.rabbitmq.producer.TerminalProducer;
import com.renewable.terminal.service.ITerminalService;
import com.renewable.terminal.util.NetIndentificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.renewable.terminal.common.constant.CacheConstant.*;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Service("iTerminalServiceImpl")
public class ITerminalServiceImpl implements ITerminalService {

	@Autowired
	private TerminalMapper terminalMapper;

	@Autowired
	private TerminalProducer terminalProducer;

	@Override
	public ServerResponse listTerminal() {
		log.info("enter listTerminal");
		List<Terminal> terminalList = terminalMapper.listTerminal();
		log.info("get terminalList");
		if (terminalList == null) {
			return ServerResponse.createByErrorMessage("can't find the terminal config on DB !");
		}
		return ServerResponse.createBySuccess(terminalList);
	}

	@Override
	public ServerResponse insertTerminal(Terminal terminal) {
		if (terminal == null) {
			return ServerResponse.createByErrorMessage("the data that want to insert is null");
		}

		int countRow = terminalMapper.insertSelective(terminal);
		if (countRow == 0) {
			return ServerResponse.createByErrorMessage("terminal insert fail !");
		}

		return ServerResponse.createBySuccess();
	}

	@Override
	public ServerResponse refreshConfigFromCent() {


		// 1.判断数据库的terminal是否有数据
		Terminal updateterminal = new Terminal();

		ServerResponse<List<Terminal>> listResponse = this.listTerminal();
		if (listResponse.getData().size() == 0) {     // 这里暂时不考虑有复数个数据的解决（即数据表由于意外情况插入多个TerminalConfig记录（话说，这个逻辑上有可能吗））
			//说明DB中没有terminalConfig
			// 2.如果没有数据，则生成一个初始化数据表记录（id为一个11位的int类型的随机数）。并将该数据插入到数据库中
			Terminal insertTerminal = this.terminalConfigGenerator();
			this.insertTerminal(insertTerminal);

			updateterminal = insertTerminal;
		}
		if (listResponse.getData().size() != 0) {
			updateterminal = listResponse.getData().get(0);
		}

		// 3.将terminal配置数据插入到GuavaCache缓存中，便于随时调用   // TerminalConfig的数据既可以从上面获取，也可以从数据库获取，这里先从数据库获取吧（这样也便于检查之前的逻辑）。
		this.setCacheByTerminalConfig(updateterminal);

		// 4.无论termianl配置数据的ID是否是随机数，都将配置数据放入MQ （这应该是一个public方法，便于随时进行数据更新）（centControl得到数据后，会根据数据的ID，判断是新生成数据记录（直接放入对应MQ，返回，进行更新），还是查询已存在数据（判断create_time，从而决定是否推送数据，进行更新）。
		this.uploadTerminalConfig(updateterminal);

		// 5.（别忘了，MQ那里需要写一个Consumer，来更新配置数据

		// 日后有时间，这里可以改为AIO模式。毕竟远程是存在网络延迟等，而本地却是实时的，也许会为调用方带来苦恼。
		// 即使由于网络情况，没有及时拿到数据，这里也有默认配置
		return ServerResponse.createBySuccessMessage("terminal config refreshed");
	}

	@Override
	public ServerResponse updateName(Terminal terminal) {
		// 1.数据校验
		if (terminal == null){
			return ServerResponse.createByErrorMessage("the terminal is null !");
		}
		if (terminal.getId() == null || terminal.getName() == null){
			return ServerResponse.createByErrorMessage("the Id or name of terminal is null ! terminal:"+terminal.toString());
		}

		// 2.简单数据Assemble
		Terminal terminalUpdate = new Terminal();
		terminalUpdate.setId(terminal.getId());
		terminalUpdate.setName(terminal.getName());
		terminalUpdate.setUpdateTime(new Date());

		// 3.更新数据
		Integer countUpdate = terminalMapper.updateByPrimaryKeySelective(terminalUpdate);
		if (countUpdate == 0){
			return ServerResponse.createByErrorMessage("terminal updateName fail !");
		}

		// 4.中控室更新
		ServerResponse centcontrolUpdateServerResponse = this.uploadTerminalConfig(terminalUpdate);
		if (centcontrolUpdateServerResponse.isFail()){
			return centcontrolUpdateServerResponse;
		}

		return ServerResponse.createBySuccessMessage("terminal updateName success .");
	}

	@Override
	public ServerResponse receiveTerminalFromRabbitmq(Terminal terminal) {
		// 这里可以先行校验缓存

		if (terminal == null) {
			return ServerResponse.createByErrorMessage("the terminal is null !");
		}

		// 更新数据库的配置
		int countRow = 0;
		if (StringUtils.isNotBlank(terminal.getMac())) {
			countRow = terminalMapper.updateByMac(terminal);     // 根据mac，对数据库记录进行更新
		}
		if (StringUtils.isBlank(terminal.getMac())) {
			countRow = terminalMapper.updateByPrimaryKeySelective(terminal);
		}

		if (countRow == 0) {
			return ServerResponse.createByErrorMessage("update terminal fail !");
		}

		// 更新缓存（只有在数据库更新成功后才可以，从而确保配置一致性（毕竟数据库出问题的可能性更大，也更重要）
		this.setCacheByTerminalConfig(terminal);
		return ServerResponse.createBySuccess("update terminal success !");
	}

	@Override
	public ServerResponse refreshCacheFromDB() {
		ServerResponse<List<Terminal>> listResponse = this.listTerminal();
		if (listResponse.getData().size() != 1) {     // 这里暂时不考虑有复数个数据的解决（即数据表由于意外情况插入多个TerminalConfig记录（话说，这个逻辑上有可能吗））
			//说明DB中没有terminalConfig
			// 2.如果没有数据，则生成一个初始化数据表记录（id为一个11位的int类型的随机数）。并将该数据插入到数据库中
			return ServerResponse.createByErrorMessage("terminal config occupired a mistake,please try it later !");
		}
		Terminal terminal = listResponse.getData().get(0);
		this.setCacheByTerminalConfig(terminal);
		return ServerResponse.createBySuccessMessage("terminal on cache refreshed");
	}

	// 2.生成随机11位ID的TerminalConfig
	private Terminal terminalConfigGenerator() {
		Terminal terminal = new Terminal();

		int randowId = (int) (Math.random() * Math.pow(10, 7) * 9 + Math.pow(10, 7));
		int projectId = 1;
		String ip = NetIndentificationUtil.getLocalIP();
		String mac = NetIndentificationUtil.getLocalMac();
		String name = "no nameded";
		int state = Const.TerminalStateEnum.Running.getCode();

		terminal.setId(randowId);
		terminal.setProjectId(projectId);
		terminal.setIp(ip);
		terminal.setMac(mac);
		terminal.setName(name);
		terminal.setState(state);
		terminal.setCreateTime(new Date());
		terminal.setUpdateTime(new Date());

		return terminal;
	}

	private void setCacheByTerminalConfig(Terminal terminal) {

		if (terminal.getId() != null) {
			GuavaCache.setKey(TERMINAL_ID, terminal.getId().toString());
		}

		if (terminal.getProjectId() != null) {
			GuavaCache.setKey(TERMINAL_PROJECT_ID, terminal.getProjectId().toString());
		}

		String terminalIp = terminal.getIp();
		if (terminalIp != null) {
			GuavaCache.setKey(TERMINAL_IP, terminalIp);
		}

		String terminalMac = terminal.getMac();
		if (terminalMac != null) {
			GuavaCache.setKey(TERMINAL_MAC, terminalMac);
		}

		String terminalName = terminal.getName();
		if (terminalName != null) {
			GuavaCache.setKey(TERMINAL_NAME, terminalName);
		}

		if (terminal.getState() != null) {
			GuavaCache.setKey(TERMINAL_STATE, terminal.getState().toString());
		}

		if (terminal.getCreateTime() != null) {
			GuavaCache.setKey(TERMINAL_CREATE_TIME, terminal.getCreateTime().toString());
		}

		if (terminal.getUpdateTime() != null) {
			GuavaCache.setKey(TERMINAL_UPDATE_TIME, terminal.getUpdateTime().toString());
		}

	}

	private ServerResponse uploadTerminalConfig(Terminal terminal) {
		System.out.println("upload terminalConfig start !");
		try {
			terminalProducer.sendTerminalConfig(terminal);
		} catch (IOException e) {
			log.error("terminal config upload fail ! " + e);
			return ServerResponse.createByErrorMessage("terminal config upload fail ! " + e);
		} catch (TimeoutException e) {
			log.error("terminal config upload fail ! " + e);
			return ServerResponse.createByErrorMessage("terminal config upload fail ! " + e);
		} catch (InterruptedException e) {
			log.error("terminal config upload fail ! " + e);
			return ServerResponse.createByErrorMessage("terminal config upload fail ! " + e);
		}
		return ServerResponse.createBySuccessMessage("terminal config upload success");
	}
}
