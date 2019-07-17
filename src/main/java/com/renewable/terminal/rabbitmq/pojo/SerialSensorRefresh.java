package com.renewable.terminal.rabbitmq.pojo;

import lombok.Data;

/**
 * @Description：
 * @Author: jarry
 */
@Data
public class SerialSensorRefresh {

	private Integer sensorRegisterId;

	private Integer terminalId;

	private String type;
}
