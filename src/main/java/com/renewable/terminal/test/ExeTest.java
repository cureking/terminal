package com.renewable.terminal.test;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * @Description：
 * @Author: jarry
 */
@Slf4j
public class ExeTest {
	public static void main(String[] args) {

		try {
			openWinExe();
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error(e.toString());
		}

//		try {
//			Runtime.getRuntime().exec("C:\\Users\\curek\\IdeaProjects\\Renewable_Power\\terminal\\audio\\tool\\wsrun.exe", null);
////			Runtime.getRuntime().exec("C:\\Users\\curek\\IdeaProjects\\Renewable_Power\\terminal\\audio\\tool\\wsrun.exe", null, new File("C:\\Users\\curek\\IdeaProjects\\Renewable_Power\\terminal\\audio\\tool\\"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public static void openWinExe() throws InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;

		try {
//			String command = "C:\\Users\\curek\\IdeaProjects\\Renewable_Power\\terminal\\audio\\tool\\wsrun.exe";
			String command = "audio\\tool\\wsrun.exe";
//			File outDir = new File("C:\\Users\\curek\\IdeaProjects\\Renewable_Power\\terminal\\audio\\tool\\");
			File outDir = new File("audio\\tool\\");
			process = runtime.exec(command, null, outDir);
		} catch (Exception e) {
			log.error("error:{}", e.toString());
		}
//
		Thread.sleep(60000);
		process.destroy();
	}
}
