package com.touchmind.work.flow.one;

import com.touchmind.work.flow.one.security.JwtProperties;
import com.touchmind.work.flow.one.manufacturing.config.ManufacturingSeedDataProperties;
import com.touchmind.work.flow.one.sample.config.SampleDataSeederProperties;
import com.touchmind.work.flow.one.sample.config.DefaultAdminSeederProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, SampleDataSeederProperties.class, ManufacturingSeedDataProperties.class, DefaultAdminSeederProperties.class})
public class WorkFlowOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkFlowOneApplication.class, args);
	}

}
