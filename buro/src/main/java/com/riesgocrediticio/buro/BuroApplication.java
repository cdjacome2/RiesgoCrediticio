package com.riesgocrediticio.buro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BuroApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuroApplication.class, args);
	}

}
