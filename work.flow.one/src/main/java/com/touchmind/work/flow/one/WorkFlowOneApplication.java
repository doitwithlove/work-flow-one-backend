package com.touchmind.work.flow.one;

import com.touchmind.work.flow.one.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WorkFlowOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkFlowOneApplication.class, args);
	}

}
