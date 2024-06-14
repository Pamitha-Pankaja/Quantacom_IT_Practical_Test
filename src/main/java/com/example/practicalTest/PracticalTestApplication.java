package com.example.practicalTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableScheduling
public class PracticalTestApplication {

	public static void main(String[] args) {
    SpringApplication.run(PracticalTestApplication.class, args);
	}

}
