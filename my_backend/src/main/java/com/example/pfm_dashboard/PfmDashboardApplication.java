package com.example.pfm_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class PfmDashboardApplication {

	@Value("${plaid.client_id}")
	private String clientId;

	@Value("${plaid.secret}")
	private String secret;

	public static void main(String[] args) {
		SpringApplication.run(PfmDashboardApplication.class, args);
	}

	@PostConstruct
	public void printEnvVariables() {
		System.out.println("Plaid Client ID: " + clientId);
		System.out.println("Plaid Secret: " + secret);
	}
}
