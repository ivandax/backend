package com.backend.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan("com.backend.demo.config")
public class BackendApplication {
	private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BackendApplication.class);
		ConfigurableApplicationContext context = app.run(args);
		String[] activeProfiles = context.getEnvironment().getActiveProfiles();
		logger.info("Active profiles: {}", Arrays.toString(activeProfiles));
	}

}
