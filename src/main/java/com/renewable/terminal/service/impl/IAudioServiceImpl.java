package com.renewable.terminal.service.impl;

import com.google.common.collect.Lists;
import com.renewable.terminal.common.GuavaCache;
import com.renewable.terminal.common.ServerResponse;
import com.renewable.terminal.extend.driven.AudioRecorder;
import com.renewable.terminal.pojo.AudioAmnout;
import com.renewable.terminal.pojo.AudioDba;
import com.renewable.terminal.service.IAudioAmnoutService;
import com.renewable.terminal.service.IAudioDbaService;
import com.renewable.terminal.service.IAudioService;
import com.renewable.terminal.util.FileUtils;
import com.renewable.terminal.util.openWinExeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.renewable.terminal.common.constant.AudioConstant.*;
import static com.renewable.terminal.common.constant.CacheConstant.TERMINAL_ID;
import static com.renewable.terminal.util.FileUtils.isFileExist;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
@Service("iAudioServiceImpl")
public class IAudioServiceImpl implements IAudioService {

	@Autowired
	private AudioRecorder audioRecorder;

	@Autowired
	private IAudioAmnoutService iAudioAmnoutService;

	@Autowired
	private IAudioDbaService iAudioDbaService;


	@Override
	public ServerResponse startupAudioSensorListTasks() {
		// 1.遍历执行所有声音传感器（目前就一个，简化一下）
		return this.startupAudioSensorTasks();
	}

	private ServerResponse startupAudioSensorTasks() {
		// 1-.关闭音频录制程序	(AudioRecorder2采用了单例模式，避免关闭错误）
		try {
			audioRecorder.stopCapture();
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.toString());
		}

		// 1.复制目标音频文件到/tool目录下
		ServerResponse fileOperationResponse = this.fileOperationWithAudioFile();
		if (fileOperationResponse.isFail()){
			return fileOperationResponse;
		}

		// 2.启动音频录制程序
		audioRecorder.startCapture();

		// 3.启动相关matlab程序
		ServerResponse dealAudioWithMatlabResponse = this.dealAudioWithMatlab();
		if (dealAudioWithMatlabResponse.isFail()){
			return dealAudioWithMatlabResponse;
		}

		// 4.处理matlab程序生成的文件
		// 4.1 dba数据采集

		ServerResponse dbaServerResponse = this.file2AudioDbaList(AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_DBA_FILE_NAME_AND_EXTENSION_NAME);
		if (dbaServerResponse.isFail()) {
			return dbaServerResponse;
		}
		List<AudioDba> audioDbaList = (List<AudioDba>) dbaServerResponse.getData();

		boolean dbaSaveResult = iAudioDbaService.saveBatch(audioDbaList);
		if (!dbaSaveResult) {
			return ServerResponse.createByErrorMessage("dbaSave fail !");
		}

		// 数据发送中控室
		iAudioDbaService.sendAudioAmnoutList2MQ((List<AudioDba>) dbaServerResponse.getData());

		// 4.2 amnout数据采集
		ServerResponse amnoutServerResponse = this.file2AudioAmnoutList(AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_AMNOUT_FILE_NAME_AND_EXTENSION_NAME);
		if (amnoutServerResponse.isFail()) {
			return amnoutServerResponse;
		}
		List<AudioAmnout> audioAmnoutList = (List<AudioAmnout>) amnoutServerResponse.getData();

		boolean amnoutSaveResult = iAudioAmnoutService.saveBatch(audioAmnoutList);
		if (!amnoutSaveResult) {
			return ServerResponse.createByErrorMessage("amnoutSave fail !");
		}

		// 数据发送中控室
		iAudioAmnoutService.sendAudioAmnoutList2MQ((List<AudioAmnout>) amnoutServerResponse.getData());

		return ServerResponse.createBySuccessMessage("the audio task has executed .");
	}

	private ServerResponse fileOperationWithAudioFile(){
		String fileName = String.valueOf(System.currentTimeMillis() / AUDIO_FILE_DURATION - 2);
		// 1.1 判断目标文件是否存在
		boolean isFileExist = isFileExist(AUDIO_ORIGIN_DIRECTORY_RELATIVE + fileName + AUDIO_FILE_EXTENSION_NAME);
		if (!isFileExist) {
			log.warn("targetFile is not exist ! filePath:{}.", AUDIO_ORIGIN_DIRECTORY_RELATIVE + fileName + AUDIO_FILE_EXTENSION_NAME);
			// 启动录音器
			audioRecorder.startCapture();
			return ServerResponse.createByErrorMessage("targetFile is not exist ! filePath:" + AUDIO_ORIGIN_DIRECTORY_RELATIVE + fileName + AUDIO_FILE_EXTENSION_NAME + " !");
		}

		File sourceFile = new File(AUDIO_ORIGIN_DIRECTORY_RELATIVE + fileName + AUDIO_FILE_EXTENSION_NAME);
		File targetFile = new File(AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_TOOL_READ_FILE_NAME + AUDIO_FILE_EXTENSION_NAME);


		// 判断目标文件夹是否存在，如果不存在就创建（针对linux，进行权限设置）
		File fileDir = new File(AUDIO_PERSISTENCE_DIRECTORY_RELATIVE);
		if (!fileDir.exists()) {
			fileDir.setWritable(true);
			fileDir.mkdirs();
		}

		try {
			FileUtils.copy(sourceFile, targetFile);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
			return ServerResponse.createByErrorMessage(e.toString());
		}


		// 1+.判断目标音频文件是否符合筛选条件（符合则放置于文件保存目录/persistence，否则直接删除）
		// 当使用两分钟间隔时，需要增加=1的判断。因为可能永远都是奇数
		if (Long.parseLong(fileName) % 60 == 0 || Long.parseLong(fileName) % 60 == 1) {
			File persistenceTargetFile = new File(AUDIO_PERSISTENCE_DIRECTORY_RELATIVE + fileName + AUDIO_FILE_EXTENSION_NAME);
			try {
				FileUtils.copy(sourceFile, persistenceTargetFile);
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString());
				return ServerResponse.createByErrorMessage(e.toString());
			}
		}
		sourceFile.delete();
		return ServerResponse.createBySuccess();
	}

	private ServerResponse dealAudioWithMatlab(){
		log.info("start matlab/wsrun.exe");
//		String targetFilePath = AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_TOOL_APPLICATION_NAME + AUDIO_TOOL_EXTENSION_NAME;
		String command = "audio\\tool\\wsrun.exe";
//		String command = targetFilePath;

//		String outputPath = AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_TOOL_APPLICATION_NAME;
		File outDir = new File("audio\\tool\\");
//		File outDir = new File(AUDIO_TOOL_DIRECTORY_RELATIVE + AUDIO_TOOL_APPLICATION_NAME);
		// 执行完matlab程序后，进行关闭
		Long destroyTime = 1000 * 60 * 2L;
//		Long destroyTime = AUDIO_MATLAB_DESTROY_DURATION;

		try {
			openWinExeUtil.openWinExe(command, outDir, destroyTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.toString());
			return ServerResponse.createByErrorMessage(e.toString());
		}
		log.info("end matlab/wsrun.exe");
		return ServerResponse.createBySuccess();
	}

	private ServerResponse file2AudioDbaList(String filePath) {
		List<AudioDba> audioDbaList = Lists.newArrayList();

		List<String> audioDbaStrList = FileUtils.readFileContent(filePath);

		for (String str : audioDbaStrList) {
			// 由于数据杂乱，所以不能很好的拆分为两个，所以要排除无效数据
			if (str.contains("NaN") || str.contains("Inf")) {
				continue;
			}
			List<String> audioDbaElementStrList = Arrays.asList(str.split(" "));
			if (audioDbaElementStrList == null || audioDbaElementStrList.size() == 0) {
				return ServerResponse.createByErrorMessage("audioDbaElementStrList is null or its size is zero !");
			}

//			audioDbaElementStrList.removeIf(Objects::isNull);
//			audioDbaElementStrList.removeIf(e -> e.isEmpty());
//			audioDbaElementStrList.removeIf(e -> e == null || e.isEmpty());
//			String[] audioDbaStrArray = str.split(" ");

			AudioDba audioDba = new AudioDba();

			audioDba.setOriginTime(Double.parseDouble(audioDbaElementStrList.get(3)));

			audioDba.setDba(Double.parseDouble(audioDbaElementStrList.get(audioDbaElementStrList.size()-1)));
			audioDba = this.audioDbaAssemble(audioDba);

			//audioDba
			audioDbaList.add(audioDba);
		}

		if (CollectionUtils.isEmpty(audioDbaList)) {
			return ServerResponse.createByErrorMessage("the audioDbaList is empty !");
		}
		return ServerResponse.createBySuccess(audioDbaList);
	}

	private ServerResponse file2AudioAmnoutList(String filePath) {
		List<AudioAmnout> audioAmnoutList = Lists.newArrayList();

		List<String> audioAmnoutStrList = FileUtils.readFileContent(filePath);

		for (String str : audioAmnoutStrList) {
			// 由于数据杂乱，所以不能很好的拆分为两个，所以要排除无效数据
			if (str.contains("NaN") || str.contains("Inf")) {
				continue;
			}

			List<String> audioAmnoutElementStrList = Arrays.asList(str.split(" "));
//			audioAmnoutElementStrList.removeIf(Objects::isNull);
//			audioAmnoutElementStrList.removeIf(e -> e.isEmpty());
//			audioAmnoutElementStrList.removeIf(e -> e == null || e.isEmpty());
//			audioAmnoutElementStrList = removeEmptyByStringListOld(audioAmnoutElementStrList);
			AudioAmnout audioAmnout = new AudioAmnout();

			// StringUtils.isNumeric()针对   6.0000000e-02   5.6515604e+01这样的科学计数法无效。由于之前已经清理无效值，此处就直接使用吧（后续可以增强此处代码）

//			if (!StringUtils.isNumeric(audioAmnoutStrArray[1])){
//				continue;
//			}
			audioAmnout.setOriginTime(Double.parseDouble(audioAmnoutElementStrList.get(3)));
//			if (!StringUtils.isNumeric(audioAmnoutStrArray[2])){
//				continue;
//			}
			audioAmnout.setAmnout(Double.parseDouble(audioAmnoutElementStrList.get(audioAmnoutElementStrList.size()-1)));

			audioAmnout = this.audioAmnoutAssemble(audioAmnout);
			audioAmnoutList.add(audioAmnout);
		}

		if (CollectionUtils.isEmpty(audioAmnoutList)) {
			return ServerResponse.createByErrorMessage("the audioAmnoutList is empty !");
		}
		return ServerResponse.createBySuccess(audioAmnoutList);
	}

	private AudioDba audioDbaAssemble(AudioDba audioDba) {
		AudioDba audioDbaResult = new AudioDba();
		BeanUtils.copyProperties(audioDba, audioDbaResult);

//		audioDba.setSensorRegisterId();
		audioDbaResult.setStatus(0);
		audioDbaResult.setTerminalId(Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)));
		Long offset = new Double((audioDbaResult.getOriginTime() * 1000L)).longValue();
		audioDbaResult.setCreateTime(new Date(System.currentTimeMillis() - AUDIO_DATE_OFFSET + offset));
		audioDbaResult.setUpdateTime(new Date(System.currentTimeMillis() - AUDIO_DATE_OFFSET+ offset));

		return audioDbaResult;
	}

	private AudioAmnout audioAmnoutAssemble(AudioAmnout audioAmnout) {

		AudioAmnout audioAmnoutResult = new AudioAmnout();
		BeanUtils.copyProperties(audioAmnout, audioAmnoutResult);

		//		audioAmnout.setSensorRegisterId();
		audioAmnoutResult.setStatus(0);
		audioAmnoutResult.setTerminalId(Integer.parseInt(GuavaCache.getKey(TERMINAL_ID)));
		Long offset = new Double((audioAmnoutResult.getOriginTime() * 1000L)).longValue();
		audioAmnoutResult.setCreateTime(new Date(System.currentTimeMillis() - AUDIO_DATE_OFFSET + offset));
		audioAmnoutResult.setUpdateTime(new Date(System.currentTimeMillis() - AUDIO_DATE_OFFSET + offset));

		return audioAmnoutResult;
	}
}
