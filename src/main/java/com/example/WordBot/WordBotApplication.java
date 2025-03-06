package com.example.WordBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories
public class WordBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(WordBotApplication.class, args);
	}
}