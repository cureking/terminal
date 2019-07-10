package com.renewable.terminal.taskDemo;

import com.renewable.terminal.extend.serial.SerialPool;
import com.renewable.terminal.rabbitmq.producer.InclinationProducer;
import com.renewable.terminal.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

	/**
	 * 用于实现轮询获取传感器监测数据
	 */
	@Scheduled(cron = "*/5 * * * * *")  //每五秒钟
	public void requireSerialData() {
		log.info("请求监测数据定时任务启动");

		iSerialSensorService.taskLoadFromSerialSensor();

		log.info("请求定时任务结束");
		System.out.println("请求定时任务结束");

	}


	@Scheduled(cron = "*/20 * * * * *") // 20秒 	// 发送倾斜数据至MQ，至中控室     //暂停，以便进行相关调试工作
	public void RabbitMQAndInclinationIntegrateService() {
		System.out.println("RabbitMQAndInclinationIntegrateService start！");

		iInclinationDealTotalService.uploadDataList();
		iInclinationDealInitService.uploadDataList();

		System.out.println("RabbitMQAndInclinationIntegrateService end");
	}

	@Scheduled(cron = "0 */1 * * * *") // 60秒	// 发送警报信息至MQ，至中控室
	public void WarningService() {
		System.out.println("RabbitMQAndInclinationIntegrateService start！");

		iWarningService.stateCheck();

		System.out.println("RabbitMQAndInclinationIntegrateService end");
	}

//	@Scheduled(cron = "0 */1 * * * *") // 60秒	// 发送警报信息至MQ，至中控室
//	public void AudioService() {
//		System.out.println("AudioService start！");
//
//		iAudioService.startupAudioSensorListTasks();
//
//		System.out.println("AudioService end");
//	}


}
