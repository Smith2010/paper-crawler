package com.mazhen.papercrawler;

import com.mazhen.papercrawler.service.SpiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PaperCrawlerApplication implements CommandLineRunner {


	@Autowired
	private SpiderService spiderService;

	public static void main(String[] args) {
		SpringApplication.run(PaperCrawlerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if ("springer".equals(args[0])) {
			spiderService.executeSpringerSpider();
		} else if ("cnki".equals(args[0])) {
			spiderService.executeCnkiSpider();
		}
	}
}
