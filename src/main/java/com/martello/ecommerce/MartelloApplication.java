package com.martello.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.martello")
public class MartelloApplication {

	public static void main(String[] args) {
		SpringApplication.run(MartelloApplication.class, args);
	}
}
