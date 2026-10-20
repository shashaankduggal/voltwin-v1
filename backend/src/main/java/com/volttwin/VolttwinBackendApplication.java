package com.volttwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolttwinBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolttwinBackendApplication.class, args);
	}

}
