package com.ljf.opencvocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class,args);
//		//不使用spring boot内置tomcat
//		new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE);
	}
	
}
