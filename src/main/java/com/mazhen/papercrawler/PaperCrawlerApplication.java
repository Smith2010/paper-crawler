package com.mazhen.papercrawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class PaperCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaperCrawlerApplication.class, args);
	}
}
