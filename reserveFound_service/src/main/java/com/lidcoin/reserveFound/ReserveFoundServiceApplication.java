package com.lidcoin.reserveFound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableFeignClients
public class ReserveFoundServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReserveFoundServiceApplication.class, args);
	}

}
