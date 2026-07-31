package com.coder.customer_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.crypto.SecretKey;

@SpringBootApplication
@EnableDiscoveryClient  // enable discovery for eureka server
public class CustomerServiceApplication {

	public static void main(String[] args) {
//		jwtUniqueKeyGenerator();
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

//	public static void jwtUniqueKeyGenerator(){
//		// Generates a secure HS256 key
//		SecretKey key = Jwts.SIG.HS256.key().build();
//		// Encodes it to Base64 so you can paste it in your YAML
//		String secretString = Encoders.BASE64.encode(key.getEncoded());
//		System.out.println("Your Base64 secret: "+secretString);
//
//	}

}
