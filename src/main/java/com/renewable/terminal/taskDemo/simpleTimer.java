package com.renewable.terminal.taskDemo;

import com.google.common.collect.Lists;
import com.renewable.terminal.extend.serial.SerialPool;
import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.rabbitmq.producer.AudioProducer;
import com.renewable.terminal.rabbitmq.producer.InclinationProducer;
import com.renewable.terminal.service.*;
import com.renewable.terminal.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author: jarry
 */
@Component
@Slf4j
public class simpleTimer {

	@Autowired
	private SerialPool serialPool;

	@Autowired
	private IInclinationService iInclinationService;

	@Autowired
	private InclinationProducer inclinationProducer;

	@Autowired
	private IInclinationDealTotalService iInclinationDealTotalService;

	@Autowired
	private IInclinationDealInitService iInclinationDealInitService;

	@Autowired
	private IWarningService iWarningService;

	@Autowired
	private ISerialSensorService iSerialSensorService;

	@Autowired
	private IAudioService iAudioService;


	@Autowired
	private AudioProducer audioProducer;

	/**
	 * 用于实现轮询获取传感器监测数据
	 */
	@Async
	@Scheduled(cron = "*/5 * * * * *")  //每五秒钟
	public void requireSerialData() {
		log.info("请求监测数据定时任务启动");

		iSerialSensorService.taskLoadFromSerialSensor();

		log.info("请求定时任务结束");
		System.out.println("请求定时任务结束");

	}

	@Async
	@Scheduled(cron = "0 */1 * * * *") // 一分钟 	// 发送倾斜数据至MQ，至中控室     //暂停，以便进行相关调试工作
	public void RabbitMQAndInclinationIntegrateService() {
		System.out.println("RabbitMQAndInclinationIntegrateService start！");

		iInclinationDealTotalService.uploadDataList();
		iInclinationDealInitService.uploadDataList();

		System.out.println("RabbitMQAndInclinationIntegrateService end");
	}

	@Async
	@Scheduled(cron = "0 */1 * * * *") // 60秒	// 发送警报信息至MQ，至中控室
	public void WarningService() {
		System.out.println("RabbitMQAndInclinationIntegrateService start！");

		iWarningService.stateCheck();

		System.out.println("RabbitMQAndInclinationIntegrateService end");
	}

	@Async
	@Scheduled(cron = "0 */2 * * * *") // 60秒	// 音频传感器进行工作
	public void AudioService() {
		System.out.println("AudioService start！");

		iAudioService.startupAudioSensorListTasks();

		System.out.println("AudioService end");
	}

}