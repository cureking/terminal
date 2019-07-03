package com.renewable.terminal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.common.constant.InitializationConstant;
import com.renewable.terminal.common.sensor.InclinationConst;
import com.renewable.terminal.dao.InclinationMapper;
import com.renewable.terminal.extend.serial.sensor.InclinationDeal526T;
import com.renewable.terminal.extend.serial.sensor.InclinationDeal826T;
import com.renewable.terminal.pojo.Inclination;
import com.renewable.terminal.pojo.InclinationDealedTotal;
import com.renewable.terminal.pojo.InitializationInclination;
import com.renewable.terminal.pojo.SerialSensor;
import com.renewable.terminal.service.IInclinationService;
import com.renewable.terminal.service.IInitializationInclinationService;
import com.renewable.terminal.service.ISerialSensorService;
import com.renewable.terminal.util.DateTimeUtil;
import com.renewable.terminal.util.OtherUtil;
import com.renewable.terminal.vo.InclinationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

//import com.renewable.gateway.serialTemp.sensor.IInclinationDeal;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Service("iInclinationServiceImpl")
public class IInclinationServiceImpl implements IInclinationService {

	@Autowired
	private InclinationMapper inclinationMapper;

	@Autowired
	private ISerialSensorService iSerialSensorService;

	@Autowired
	private IInitializationInclinationService iInitializationInclinationService;

//    @Autowired
//    private SensorRegisterMapper sensorRegisterMapper;

	@Override
	public ServerResponse<PageInfo> listInclinationData(int pageNum, int pageSize) {
		//pagehelper 使用逻辑:  第一步，startPage--start；第二步，填充自己的sql查询逻辑；第三步，pageHelper--收尾
		//第一步：startPage--start
		PageHelper.startPage(pageNum, pageSize);

		//第二步：填充自己的sql查询逻辑
		List<Inclination> inclinationList = inclinationMapper.selectList();
		//todo_finished 数据全部为空
		if (inclinationList == null) {
			return ServerResponse.createByErrorMessage("no data conform your requirement!");
		}

		List<InclinationVo> inclinationVoList = Lists.newArrayList();
		for (Inclination inclinationItem : inclinationList) {
			InclinationVo inclinationVo = assumbleInclinationListVo(inclinationItem);
			inclinationVoList.add(inclinationVo);
		}

		//第三步：pageHelper--收尾
		PageInfo pageResult = new PageInfo(inclinationList);
		pageResult.setList(inclinationVoList);
		return ServerResponse.createBySuccess(pageResult);
	}

	@Override
	public ServerResponse<List<Object>> listInclinationDataByTime(String startTime, String endTime) {
		List<Inclination> inclinationList = inclinationMapper.selectListByTime(DateTimeUtil.strToDate(startTime), DateTimeUtil.strToDate(endTime));
		List<Object> inclinationVoObjectList = Lists.newArrayList();
		for (Inclination inclinationItem : inclinationList) {
			InclinationVo inclinationVo = assumbleInclinationListVo(inclinationItem);
			Object InclinationVoObject = (Object) inclinationVo;
			inclinationVoObjectList.add(InclinationVoObject);

		}
		return ServerResponse.createBySuccess(inclinationVoObjectList);
	}

	@Override
	public void sendDataInclination(String port, String baudrate, InclinationConst.InclinationSensor1Enum inclinationSensor1Enum) {
		System.out.println("IInclinationSerciveImpl/sendDataInclination");
		//todo 其实底层并没有采用baudrate，因为这需要动态管理，当时还没有设备注册表，故采用统一的波特率
//        iInclinationDeal.sendData("COM1",9600,01, InclinationConst.InclinationSensor1Enum.READALL);

	}

	/**
	 * 用于将serialListener接收到的数据转为标准格式，并计算相关综合值，最后写入数据库。
	 *
	 * @param port
	 * @param baudrate
	 * @param originBuffer
	 * @return
	 */
	@Override
	public ServerResponse receiveData(String port, int baudrate, byte[] originBuffer) {
		//要对获得的name（即port进行一定处理.）
		port = OtherUtil.getSubStrByCharAfter(port, '/');

		// 在这里设置数据转换的规则
		ServerResponse<Inclination> serverResponse = null;
		// 826T
		if (InclinationConst.InclinationSerialTypeEnum.T826.getCode() == originBuffer.length) {
			serverResponse = InclinationDeal826T.origin2Object(originBuffer);
		}
		// 526T
		if (InclinationConst.InclinationSerialTypeEnum.T526.getCode() == originBuffer.length) {
			serverResponse = InclinationDeal526T.origin2Object(originBuffer);
		}


		if (serverResponse == null) {
			return ServerResponse.createByErrorMessage("can't find a sensor with the data array:" + Arrays.toString(originBuffer));
		}

		if (!serverResponse.isSuccess()) {
			return ServerResponse.createByErrorMessage("data transfer error:" + Arrays.toString(originBuffer));
		}

		Inclination inclination = serverResponse.getData();
		//放入计算后，不含初始角度的合倾角与对应方位角
		double[] resultTDArray = calAngleTotal(inclination.getAngleX(), inclination.getAngleY());
		inclination.setAngleTotal(resultTDArray[0]);
		inclination.setDirectAngle(resultTDArray[1]);

		//获取算法三所需参数（调用算法二matlab版）   //todo 其实这里应该是根据port与address的，但是 获取的数据还没有address。。当然上一层的数据是有的，只是没有传下来，之后有时间弄一下。
		System.out.println("port:" + port + " baudrate:" + baudrate);
		// TODO 这里不要硬编码
		StringBuilder address = new StringBuilder("00");
		ServerResponse serialSensorResponse = iSerialSensorService.getSerialSensorByPortAndAddress(port, address.toString());
		if (serialSensorResponse.isFail()) {
			return ServerResponse.createByErrorMessage("can't get the serialSensor with port: " + port + " address: " + address.toString());
		}
		SerialSensor serialSensor = (SerialSensor) serialSensorResponse.getData();

		if (serialSensor == null) {
			return ServerResponse.createByErrorMessage("can't get the serialSensor with port: " + port + " address: " + address.toString());
		}
		int sensorRegisterId = serialSensor.getSensorRegisterId();
		ServerResponse initializationInclinationResponse = iInitializationInclinationService.getInitializationInclinationBySensorRegisterId(sensorRegisterId);
		if (initializationInclinationResponse.isFail()) {
			return ServerResponse.createByErrorMessage("can't get the initializationInclinationResponse with sensorRegisterId: " + sensorRegisterId);
		}
		InitializationInclination initializationInclination = (InitializationInclination) initializationInclinationResponse.getData();

		// 判断初始配置表的状态，决定是否进行计算
		if (initializationInclination.getRadius() == null ||
				initializationInclination.getInitH1() == null || initializationInclination.getInitAngle1() == null || initializationInclination.getInitH2() == null || initializationInclination.getInitAngle2() == null ||
				initializationInclination.getInitH3() == null || initializationInclination.getInitAngle3() == null || initializationInclination.getInitH4() == null || initializationInclination.getInitAngle4() == null ||
				initializationInclination.getTotalAngleLimit() == null || initializationInclination.getTotalInitAngleLimit() == null ||
				initializationInclination.getInitX() == null || initializationInclination.getInitY() == null) {
			// 以后可以改变表内state字段，不用每次的都判断
			log.error("initializationInclination with serialSensorId = {} is null ! please input theinitializationInclination", ((SerialSensor) serialSensorResponse.getData()).getId());
			return ServerResponse.createByErrorMessage(InitializationConstant.InitializationStatusEnum.codeOf(InitializationConstant.InitializationStatusEnum.USE.getCode()).getValue());
		}

		double R = initializationInclination.getRadius();
		double[][] initMeasuerArray = OtherUtil.assembleMatlabArray(
				initializationInclination.getInitH1(), initializationInclination.getInitH2(), initializationInclination.getInitH3(), initializationInclination.getInitH4(),
				initializationInclination.getInitAngle1(), initializationInclination.getInitAngle2(), initializationInclination.getInitAngle3(), initializationInclination.getInitAngle4());


		//开始算法三的计算          //InclinationConst.InclinationInstallModeEnum.FOUR
		double[] resultTDInitArray = this.calInitAngleTotal(inclination.getAngleX(), inclination.getAngleY(), initializationInclination.getInitX(), initializationInclination.getInitY(), initializationInclination.getInitTotalAngle(), InclinationConst.InclinationInstallModeEnum.codeOf(1));


		//放入计算后，包含初始角度的合倾角，极其对应的方位角
		inclination.setAngleInitTotal(resultTDInitArray[0]);
		inclination.setDirectAngleInit(resultTDInitArray[1]);
		//日后需要，这里可以扩展加入方向角  //方向角的计算可以查看ipad上概念化画板/Renewable/未命名8（包含方位角的计算）
		//添加该数据对应的sensorID
		inclination.setSensorId(initializationInclination.getSensorRegisterId());
		inclination.setVersion("NoClean");


		//数据处理由sql完成
		ServerResponse response = inclinationData2DB(inclination);
		return response;
	}

	private double[] calAngleTotal(double angleX, double angleY) {
		double[] angleTotal = null;

		if (angleX == 0 && angleY == 0) {    //这样的条件看起来较为更为清晰，即计算公式无法计算X=Y=0的情况。
			//可以不做处理
		} else {
			angleTotal = OtherUtil.calAngleTotal(angleX, angleY, 0, 1);
		}
		return angleTotal;
	}

	@Override
	public ServerResponse inclinationData2DB(Inclination inclination) {
		System.out.println("angle_X:" + inclination.getAngleX() + "  angle_Y:" + inclination.getAngleY() + "  temperature:" + inclination.getTemperature());

		inclination.setCreateTime(new Date());

		int resutlt = inclinationMapper.insertSelective(inclination);
		if (resutlt == 0) {
			return ServerResponse.createByErrorMessage("insert inclinationData failure!");
		}
		return ServerResponse.createBySuccessMessage("insert inclinationData success");
	}


	private InclinationVo assumbleInclinationListVo(Inclination inclination) {
		InclinationVo inclinationVo = new InclinationVo();
		inclinationVo.setId(inclination.getId());
		inclinationVo.setAngleX(inclination.getAngleX());
		inclinationVo.setAngleY(inclination.getAngleY());
		inclinationVo.setTemperature(inclination.getTemperature());
		inclinationVo.setCreateTime(inclination.getCreateTime());

		//还可以添加一些公共部分，如url中的domain

		return inclinationVo;
	}

	private InclinationDealedTotal inclinationDealAssemble(Inclination inclination) {
		InclinationDealedTotal inclinationDealedTotal = new InclinationDealedTotal();
		inclinationDealedTotal.setOriginId(inclination.getId());
		inclinationDealedTotal.setSensorId(inclination.getSensorId());
		inclinationDealedTotal.setAngleX(inclination.getAngleX());
		inclinationDealedTotal.setAngleY(inclination.getAngleY());
		inclinationDealedTotal.setAngleTotal(inclination.getAngleTotal());
		inclinationDealedTotal.setTemperature(inclination.getTemperature());
		return inclinationDealedTotal;
	}

	/**
	 * 有关风机初始倾角测量的计算
	 *
	 * @param initMeasureArray //  double[][] initMeasureArray  double p1h,double p1angel,double p2h,double p2angel,double p3h,double p3angel,double p4h,double p4angel
	 * @param R
	 * @return 返回数组的第一个元素表示合倾角，第二个元素表示其方位角。
	 */
	@Override
	public double[] initTotalAngleCal(double[][] initMeasureArray, double R) {
		double initTotalAngle = 0;
		double initTotalDirectAngle = 0;
		double[] singlePlaneResultArray;

		//这里先硬编码，之后进行抽象，重构。
		double[][] singlePlaneArray1 = {initMeasureArray[1], initMeasureArray[2], initMeasureArray[3]};
		singlePlaneResultArray = initPlaneAngle(singlePlaneArray1, R);
		if (initTotalAngle < singlePlaneResultArray[0]) {
			initTotalAngle = singlePlaneResultArray[0];
			initTotalDirectAngle = singlePlaneResultArray[1];
		}
//        System.out.println(Arrays.toString(singlePlaneResultArray));
		double[][] singlePlaneArray2 = {initMeasureArray[0], initMeasureArray[2], initMeasureArray[3]};
		singlePlaneResultArray = initPlaneAngle(singlePlaneArray2, R);
		if (initTotalAngle < singlePlaneResultArray[0]) {
			initTotalAngle = singlePlaneResultArray[0];
			initTotalDirectAngle = singlePlaneResultArray[1];
		}
//        System.out.println(Arrays.toString(singlePlaneResultArray));
		double[][] singlePlaneArray3 = {initMeasureArray[0], initMeasureArray[1], initMeasureArray[3]};
		singlePlaneResultArray = initPlaneAngle(singlePlaneArray3, R);
		if (initTotalAngle < singlePlaneResultArray[0]) {
			initTotalAngle = singlePlaneResultArray[0];
			initTotalDirectAngle = singlePlaneResultArray[1];
		}
//        System.out.println(Arrays.toString(singlePlaneResultArray));
		double[][] singlePlaneArray4 = {initMeasureArray[0], initMeasureArray[1], initMeasureArray[2]};
		singlePlaneResultArray = initPlaneAngle(singlePlaneArray4, R);
		if (initTotalAngle < singlePlaneResultArray[0]) {
			initTotalAngle = singlePlaneResultArray[0];
			initTotalDirectAngle = singlePlaneResultArray[1];
		}
//        System.out.println(Arrays.toString(singlePlaneResultArray));

		double[] totalAngleArray = {initTotalAngle, initTotalDirectAngle};
		return totalAngleArray;
	}


	//    计算三个点组成的平面的初始倾角
//    返回数组的第一个元素表示合倾角，第二个元素表示其方位角。
//    double p1h,double p1angel,double p2h,double p2angel,double p3h,double p3angel         singlePlaneResultArray[high,angle]
	private double[] initPlaneAngle(double[][] singlePlaneArray, double R) {
//        for (double[] doubles : singlePlaneArray) {
//            System.out.println(Arrays.toString(doubles));
//        }
		//要将三个点按高程从小到大排序
		singlePlaneArray = OtherUtil.bubbleSort(singlePlaneArray);  //默认是按0位排序的 //这里采用的是冒泡排序，之后可以改进
//        for (double[] doubles : singlePlaneArray) {
//            System.out.println(Arrays.toString(doubles));
//        }

		//提取出需要的参数  //其实可以直接写，但为了后来者可以看懂，还是分开写吧。
		double p1h = singlePlaneArray[0][0];
		double p1angel = singlePlaneArray[0][1];
		double p2h = singlePlaneArray[1][0];
		double p2angel = singlePlaneArray[1][1];
		double p3h = singlePlaneArray[2][0];
		double p3angel = singlePlaneArray[2][1];

		//将角度转为弧度制。
//        System.out.println("angle:"+p1angel);
		p1angel = OtherUtil.angle2radian(p1angel);
//        System.out.println("radian:"+p1angel);
		p2angel = OtherUtil.angle2radian(p2angel);
		p3angel = OtherUtil.angle2radian(p3angel);

		double F = Math.sin(p2angel - p3angel)
				+ Math.sin(p3angel - p1angel)
				+ Math.sin(p1angel - p2angel);
		double Fx = (Math.sin(p3angel) - Math.sin(p2angel)) * p1h +
				(Math.sin(p1angel) - Math.sin(p3angel)) * p2h +
				(Math.sin(p2angel) - Math.sin(p1angel)) * p3h;

		double Fy = (Math.cos(p3angel) - Math.cos(p2angel)) * p1h +
				(Math.cos(p1angel) - Math.cos(p3angel)) * p2h +
				(Math.cos(p2angel) - Math.cos(p1angel)) * p3h;
//        System.out.println("Fy start:");
//        System.out.println((Math.cos(p3angel)-Math.cos(p2angel))*p1h);
//        System.out.println((Math.cos(p1angel)-Math.cos(p3angel))*p2h);
//        System.out.println((Math.cos(p2angel)-Math.cos(p1angel))*p3h);
//        System.out.println("Fy end!");
		double X = Fx / (F * R);
		double Y = Fy / (F * R);

		double totalAngle = Math.acos(1 / Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + 1));      //合倾角计算
		double totalDirectAngle;
		if (Y > 0) {
			totalDirectAngle = Math.acos(-X / (Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2))));
		} else if (Y < 0) {
			totalDirectAngle = 2 * Math.PI - Math.acos(-X / (Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2))));
		} else {
			//Y == 0的情况下
			totalDirectAngle = Math.PI;   //TODO 需要修改，这里是随便写的初始值。
		}
		//方位角范围是[0,360)的
		totalDirectAngle = (totalDirectAngle == Math.PI * 2 ? 0 : totalDirectAngle);
//
//        System.out.println("pre-p1:"+p1angel+" pre-p2:"+p2angel+" pre-p3:"+p3angel);
//        System.out.println("F:"+F+" Fx:"+Fx+" Fy:"+Fy+" X:"+X+" Y:"+Y);
//        System.out.println("totalAngle:"+OtherUtil.radian2angle(totalAngle)+" totlaDirectAngle:"+OtherUtil.radian2angle(totalDirectAngle));

		double[] totalAngleArray = {OtherUtil.radian2angle(totalAngle), OtherUtil.radian2angle(totalDirectAngle)};
		return totalAngleArray;
	}

	@Override
	public double[] calInitAngleTotal(double angleX, double angleY, double X, double Y, double angleInitTotal, InclinationConst.InclinationInstallModeEnum installModeEnum) {
		return OtherUtil.calInitAngleTotal(angleX, angleY, X, Y, angleInitTotal, installModeEnum);

	}

//	@Override
//	public ServerResponse taskLoadInclinationFromSensor() {
//		//TODO 其实这边的逻辑可以实现自动化，就是利用function_type访问sensor，从而确定倾斜传感器列表（保存至缓存），再利用sensorId从sensorRegister中确定snesorRegisterId的列表。最后将对应serialSensor保存至缓存即可（注意刷新）
//		// 现在就直接访问serial表（目前用串口的，只有倾斜）。之所以从这里调用，也只是为之后保留一份扩展性
//		// 又想了想，serialSensor的执行与它的功能有关嘛？无关。只要发送正确的命令即可，接收数据的地方有专门的处理
//
//		// 之后调用参数为senrialSensorIdList的方法
//		ServerResponse loadSerialSensorResponse = iSerialSensorService.taskLoadFromSerialSensor();
//
//		return null;
//	}

}