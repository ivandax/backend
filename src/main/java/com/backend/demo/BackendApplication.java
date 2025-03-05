package com.backend.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan("com.backend.demo.config")
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BackendApplication.class);
		Environment env = app.run(args).getEnvironment();
		System.out.println("My active profiles: " + Arrays.toString(env.getActiveProfiles()));
	}

}
