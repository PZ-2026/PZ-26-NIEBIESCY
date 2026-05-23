/*
 * BackendApplication.java
 *
 * Version: 1.1
 * Date: 2026-05-16
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the EduLink backend Spring Boot application.
 *
 * @version 1.1
 * @author EduLink Team
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	/**
	 * Launches the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
