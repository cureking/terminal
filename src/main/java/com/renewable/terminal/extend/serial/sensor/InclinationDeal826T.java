package com.renewable.terminal.extend.serial.sensor;

import com.google.common.collect.Lists;
import com.renewable.terminal.common.Const;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.common.sensor.InclinationConst;
import com.renewable.terminal.pojo.Inclination;
import com.renewable.terminal.util.HexUtil;
import com.renewable.terminal.util.OtherUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static com.renewable.terminal.util.OtherUtil.bcdArray2intArray;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
public class InclinationDeal826T {

	public static ServerResponse origin2Object(byte[] originBuffer) {

		//源数据数组转换：signed2unsigned   //但是除了数据域，还有别的部分
//		System.out.println(Arrays.toString(originBuffer));

		//对Byte的命令字数据进行补位处理（确保为正，便于处理）
		int command = originBuffer[3] & 0xFF;
		//接收数据数据处理规则switch
		if (command == InclinationConst.InclinationSensor1Enum.READALLR.getCode()) {
			//应答制下原始数据响应，同时也是自动输出模式下数据响应
			//todo 暂时不做明确相应，没有时间，且优先级不够。

			//2.对接收到的检测参数进行处理
			Inclination inclination = originData2Object(originBuffer);
			return ServerResponse.createBySuccess("acquire the readData:", inclination);


			//TODO 其他命令的响应处理页放在后面，不要急，重要程度不高的。
		} else if (command == InclinationConst.InclinationSensor1Enum.SETZEROR.getCode()) {
			//call the relative service. e.g:Sensor_registry.
			return ServerResponse.createByErrorMessage("this origin dataArray is SETZEROR");
		} else if (command == InclinationConst.InclinationSensor1Enum.SETSPEEDR.getCode()) {

			return ServerResponse.createByErrorMessage("this origin dataArray is SETSPEEDR");
		} else if (command == InclinationConst.InclinationSensor1Enum.SETMODR.getCode()) {

			return ServerResponse.createByErrorMessage("this origin dataArray is SETMODR");
		} else if (command == InclinationConst.InclinationSensor1Enum.SETADDRR.getCode()) {

			return ServerResponse.createByErrorMessage("this origin dataArray is SETADDRR");
		} else if (command == InclinationConst.InclinationSensor1Enum.GETZEROR.getCode()) {

			return ServerResponse.createByErrorMessage("this origin dataArray is GETZEROR");
		} else { //2.无法解析数据
			log.error("can't exact this data package:", Arrays.toString(originBuffer));
		}

		return ServerResponse.createByErrorMessage("can't exact this data package:" + Arrays.toString(originBuffer));
	}


	public static ServerResponse<byte[]> command2Origin(int address, InclinationConst.InclinationSensor1Enum inclinationSensor1Enum) {
		byte[] originData;

		if (inclinationSensor1Enum == null) {
			return ServerResponse.createByErrorMessage("the command is null");
		}

		//1. 生成对应数据帧指令
		int command = inclinationSensor1Enum.getCode();

		if (command == InclinationConst.InclinationSensor1Enum.READALL.getCode()) {
			//生成指令数据帧
			originData = list2OriginData(objece2ListData(command, address));
		} else if (command == InclinationConst.InclinationSensor1Enum.GETZERO.getCode()) {
			originData = list2OriginData(objece2ListData(command, address));
		} else {
			log.warn("生成命令时，发生异常 ", command);
			return ServerResponse.createByErrorMessage("can't find the command type in No-Data_command2Origin method.command = " + command);
		}

		return ServerResponse.createBySuccess("the command2Origin is OK.", originData);
	}

	public static ServerResponse<byte[]> command2Origin(int address, InclinationConst.InclinationSensor1Enum inclinationSensor1Enum, String data) {
		byte[] originData;

		if (inclinationSensor1Enum == null) {
			//也许这里可以新建一种返回响应格式，如ResponseServer那样
			return ServerResponse.createByErrorMessage("the command is null");
		}

		//1. 生成对应数据帧指令
		int command = inclinationSensor1Enum.getCode();

		if (command == InclinationConst.InclinationSensor1Enum.SETZERO.getCode()) {
			//生成指令数据帧   todo 还没修改
			originData = list2OriginData(objece2ListData(command, address));
		} else if (command == InclinationConst.InclinationSensor1Enum.GETZERO.getCode()) {

			originData = list2OriginData(objece2ListData(command, address));
		} else {
			log.warn("生成命令时，发生异常 ", command);
			return ServerResponse.createByErrorMessage("can't find the command type in Data_command2Origin method.command = " + command);
		}

		return ServerResponse.createBySuccess("the command2Origin is OK.", originData);
	}


	//todo 日后这个转换要做成相应工具包的。
	private static Inclination originData2Object(byte[] originData) {
		Inclination inclination = new Inclination();
		//这里暂时先利用硬编码实现，但很明显这并不优雅，之后优化处理。而且这里代码的健壮性极差，完全无法应对任何意料之外的情况，如outofarrayrangeexception.etc
		double origin_X = origin2Double(OtherUtil.subBytes(originData, 4, 4));
		double origin_Y = origin2Double(OtherUtil.subBytes(originData, 8, 4));
		double origin_T = origin2Double(OtherUtil.subBytes(originData, 12, 4));
//		System.out.println("originData:" + Arrays.toString(originData));
//		System.out.println("subByte:" + Arrays.toString(OtherUtil.subBytes(originData, 7, 3)));
		inclination.setAngleX(origin_X);
		inclination.setAngleY(origin_Y);
		inclination.setTemperature(origin_T);
		//该数据的create_time由mybatis的mapper中now()函数控制
		System.out.println("origin_X:" + origin_X);
		System.out.println("origin_Y:" + origin_Y);
		System.out.println("origin_T:" + origin_T);
		return inclination;
	}

	/**
	 * List<Integer>转为对应2字节Byte[]类型 （容量扩大一倍）
	 *
	 * @param list
	 * @return
	 */
	private static byte[] list2OriginData(List<Integer> list) {
		byte[] originData = new byte[list.size()];
		int i = 0;
		for (Integer integer : list) {
//            originData[i] = int2byte(integer);        很想骂人，serialPort是会自动转的，不需要我们处理。（那你倒是标明啊。要么就别用那么具有误会性的参数列表啊）
			originData[i] = int2byteNoChange(integer);
			i++;
		}

//        Byte[] originData =list.toArray(new Byte[list.size()]);       //原生方法无法使用
		return originData;
	}

	/**
	 * short整数转换为2字节的byte数组
	 *
	 * @param s short整数
	 * @return byte数组
	 */
	private static Byte[] unsignedShortToByte2(int s) {
		Byte[] targets = new Byte[2];
		targets[0] = (byte) (s >> 8 & 0xFF);
		targets[1] = (byte) (s & 0xFF);
		return targets;
	}

	private static byte int2byte(int data) {
		String hex = Integer.toHexString(data);
		return Byte.parseByte(hex);
	}

	private static byte int2byteNoChange(int data) {
		String hex = Integer.toString(data);
		return Byte.parseByte(hex);
	}

	private static List<Integer> objece2ListData(int command, int address) {

		//数据先以Integer类型保存，之后转为Byte
		List<Integer> originArray = Lists.newArrayList();

		//这里可以解耦
		int data_0 = Const.SensorEnum.Inclination1.getCode();
		int data_1 = 4;
		int data_2 = address;
		int data_3 = command;
		int data_5 = 0;

		originArray.add(data_0);
		originArray.add(data_1);
		originArray.add(data_2);
		originArray.add(data_3);

		//设置数据长度（+1是因为数据校验位还没有添加，需要计算）（-1是因为标示符不参与计算）
		originArray.set(1, originArray.size() + 1 - 1);

		//计算校验和     校验和是不计算标示符（不计算第一位）与自身的（不计算最后一位）
		for (int j = 1; j < originArray.size(); j++) {
			data_5 += originArray.get(j);
		}
		originArray.add(data_5);
		return originArray;
	}

	/**
	 * 但是我改完又后悔了。先不提改后有点难看，这样写就等于将判断data是否为空交由上层处理，上层必须完成函数的挑选。
	 * 不过上层还要完成data的建立，所以这里的统一整合，放在之后再细致优化吧。
	 *
	 * @param command
	 * @param address
	 * @param data
	 * @return
	 */
	//todo_finished 这里可以对方法进行重载，毕竟有的命令是不存在数据域的。
	private static List<Integer> objece2ListData(int command, int address, Integer data) {

		//数据先以Integer类型保存，之后转为Byte
		List<Integer> originArray = Lists.newArrayList();

		//这里可以解耦
		int data_0 = Const.SensorEnum.Inclination1.getCode();
		int data_1 = 5;
		int data_2 = address;
		int data_3 = command;
		int data_4 = data;
		int data_5 = 0;

		originArray.add(data_0);
		originArray.add(data_1);
		originArray.add(data_2);
		originArray.add(data_3);
		originArray.add(data_4);

		//设置数据长度（+1是因为数据校验位还没有添加，需要计算）
		originArray.set(1, originArray.size() + 1);

		//计算校验和     校验和是不计算标示符（不计算第一位）与自身的（不计算最后一位）
		for (int j = 1; j < originArray.size(); j++) {
			data_5 += originArray.get(j);
		}
		originArray.add(data_5);
		return originArray;
	}

	/**
	 * 完成三个（也许应该扩展为多个，再做成工具Util）十六进制转double（包含合并）
	 * 这里已经在方法中预留了三个变量，用于日后扩展
	 *
	 * @param originData
	 * @return
	 */
	private static Double origin2Double(byte[] originData) {
//        byte[] originDataSplit = OtherUtil.arraySplitByByte(originData);
		byte[] originDataSplit = OtherUtil.arraySplitByInt(bcdArray2intArray(originData));
		double result = 0;
//		System.out.println(Arrays.toString(originDataSplit));

		int signedCount = 1;
		int integerCount = 3;
		int decimalCount = 4;

		//数据符号与高位
		int signedValue = (originDataSplit[0] >= 1) ? -1 : 1;

		//整数域部分
		int integerValue = HexUtil.bcd2int(OtherUtil.subBytes(originDataSplit, signedCount, integerCount));
//        System.out.println(Arrays.toString(originDataSplit));
		//小数域
		int decimalValue = HexUtil.bcd2int(OtherUtil.subBytes(originDataSplit, signedCount + integerCount, decimalCount));

		result = signedValue * (integerValue + decimalValue * Math.pow(10, -decimalCount));
//        System.out.println("result:" + result);
		return result;
	}

	private static Byte[] unsignedShortToByte2(byte s) {
		Byte[] targets = new Byte[2];
		targets[0] = (byte) (s >> 8 & 0xFF);
		targets[1] = (byte) (s & 0xFF);
		return targets;
	}

	public static void main(String[] args) {

		ServerResponse<byte[]> serverResponse = command2Origin(00, InclinationConst.InclinationSensor1Enum.READALL);

		byte[] originData = {104, 16, 0, -124, 16, 0, -108, 101, 0, 0, 66, 117, 0, 52, 54, 0, -66};

//		System.out.println(Arrays.toString((OtherUtil.subBytes(originData,8,4))));
		System.out.println(origin2Double((OtherUtil.subBytes(originData, 8, 4))));

		byte[] test4 = {16, 0, -108, 101};
		byte[] test5 = {0, 0, 66, 117};
		byte[] test6 = {0, 52, 54, 0};

		System.out.println(origin2Double(test4));


	}
}
