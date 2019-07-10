package com.renewable.terminal.extend.driven;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.File;

import static com.renewable.terminal.common.constant.AudioConstant.AUDIO_CLEAN_DURATION;
import static com.renewable.terminal.common.constant.AudioConstant.AUDIO_FILE_DURATION;

/**
 * @Description：
 * @Author: jarry
 */
@Component
@Slf4j
public class AudioRecorder3 {

	private static final long serialVersionUID = 1L;

	private static AudioFormat audioFormat;
	private static TargetDataLine targetDataLine;

//	@Bean
//	public static AudioRecorder AudioRecorder(){
//		return new AudioRecorder();
//	}

	/**
	 * 开始声音抓取（新建了一个DataLine）
	 */
	private void captureAudio() {
		try {
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

			// 新建CaptureThread()，这里是多线程的任务（也许1分钟时长应该设置在这里）
			Thread audioThread = new CaptureThread();
			audioThread.start();
			log.info("audio capture start");

			Thread.sleep(AUDIO_FILE_DURATION);
			targetDataLine.stop();
			targetDataLine.close();
			audioThread.interrupt();
			log.info("audio capture end");

		} catch (Exception e) {
			e.printStackTrace();
			// 0表示正常退出当前虚拟机（当然服务器是不可以这样的，关了虚拟机，服务器还咋办。。。）
			// System.exit(0);
			log.error(e.toString());
		}
	}

	/**
	 * 对过去声音的格式进行设置（设定频率，比特位等）
	 *
	 * @return
	 */
	private AudioFormat getAudioFormat() {
		float sampleRate = 8000.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 2;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}

	/**
	 * 新开一个线程，用于抓取声音数据，并在其中设置了数据保存的格式与路径
	 */
	class CaptureThread extends Thread {
		@Override
		public void run() {
			AudioFileFormat.Type fileType = null;
			File audioFile = null;

			// 这里我们先简单一些。首先由于是本地文件操作，故不建立文件服务器，其次，这里建立简单目录
			fileType = AudioFileFormat.Type.WAVE;

			// 以小时为每分钟文件的标志
			String fileName = "audio_" + String.valueOf(System.currentTimeMillis() / AUDIO_CLEAN_DURATION);
			String fileExtension = ".wav";

//			audioFile = new File("junk.wav");
			String path = "audio";
			File fileDir = new File(path);
			if (!fileDir.exists()) {
				fileDir.setWritable(true);
				fileDir.mkdirs();
			}
			audioFile = new File(path, fileName + fileExtension);

			try {
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				AudioSystem.write(new AudioInputStream(targetDataLine), fileType, audioFile);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}


	public static void main(String args[]) {
		AudioRecorder3 audioRecorder = new AudioRecorder3();
		audioRecorder.captureAudio();
	}// end main

}

