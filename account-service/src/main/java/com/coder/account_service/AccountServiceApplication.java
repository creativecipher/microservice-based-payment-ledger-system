package com.coder.account_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
//		SpringApplication app = new SpringApplication(CmsApplication.class);
//
//		app.addListeners((ApplicationFailedEvent event) -> {
//			System.err.println("SPRING FAILED");
//			event.getException().printStackTrace();
//		});
//		app.run(args);
	}

//	@Bean
//	public CommandLineRunner debugEnv(Environment env){
//		return args -> {
//			System.out.println(">>> DB_USER = "+env.getProperty("DB_USER"));
//			System.out.println(">>> env.DB_USER = "+env.getProperty("env.DB_USER"));
//			System.out.println(">>> DB_PASSWORD = "+env.getProperty("DB_PASSWORD"));
//			System.out.println(">>> env.DB_PASSWORD = "+env.getProperty("env.DB_PASSWORD"));
//		};
//	}
}
