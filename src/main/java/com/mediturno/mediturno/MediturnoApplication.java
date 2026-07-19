package com.mediturno.mediturno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediturnoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediturnoApplication.class, args);
	}

}
