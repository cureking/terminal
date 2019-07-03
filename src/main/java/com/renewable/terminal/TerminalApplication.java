package com.renewable.terminal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableScheduling   // 用于启动Spring_Schedule
@SpringBootApplication
@MapperScan("com.renewable.terminal.dao")    // 用于扫描dao层
@EnableWebMvc
public class TerminalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TerminalApplication.class, args);
	}

}
