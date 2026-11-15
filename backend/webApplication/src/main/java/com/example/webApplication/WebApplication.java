package com.example.webApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {

		Dotenv.configure()
			.directory("./")
			.systemProperties()
			.load();
			
		SpringApplication.run(WebApplication.class, args);
	}

}
