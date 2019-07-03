package com.renewable.terminal.exception.serial;

public class SendDataToSerialPortFailure extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public SendDataToSerialPortFailure() {
	}

	@Override
	public String toString() {
		return "数据发送端口失败";
	}

}
