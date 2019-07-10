package com.renewable.terminal.common.constant;

/**
 * @Description：
 * @Author: jarry
 */
public class AudioConstant {

	// 每个音频文件的时长	// 1分钟		// 加上括号，避免等价替换带来的计算式问题
	public static final int AUDIO_FILE_DURATION = (1000 * 60);
	// 音频文件的采样周期	// 1小时
	public static final int AUDIO_CLEAN_DURATION = (1000 * 60 * 60);

	public static final String AUDIO_ORIGIN_DIRECTORY_RELATIVE = "audio\\";
	public static final String AUDIO_TOOL_DIRECTORY_RELATIVE = "audio\\tool\\";
	public static final String AUDIO_PERSISTENCE_DIRECTORY_RELATIVE = "audio\\persistence\\";
	public static final String AUDIO_TOOL_READ_FILE_NAME = "wstest";
	public static final String AUDIO_FILE_EXTENSION_NAME = ".wav";
	public static final String AUDIO_TOOL_APPLICATION_NAME = "wsrun";
	public static final String AUDIO_TOOL_EXTENSION_NAME = ".exe";

	public static final String AUDIO_DBA_FILE_NAME_AND_EXTENSION_NAME = "dBA.txt";
	public static final String AUDIO_AMNOUT_FILE_NAME_AND_EXTENSION_NAME = "AMNout.txt";
}
