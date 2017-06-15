package com.mazhen.papercrawler.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

/**
 * Created by smithma on 31/05/2017.
 */
@Slf4j
@Service
public class ScheduledJobService {

	@Autowired
	@Qualifier("springerSpider")
	private Spider springerSpider;

	@Autowired
	@Qualifier("cnkiSpider")
	private Spider cnkiSpider;

	@Scheduled(cron = "0 0 1 * * ?")
	public void executeSpringerSpider() {
		StopWatch watch = new StopWatch();
		watch.start();

		springerSpider.run();

		watch.stop();
		log.info("Fetch Springer data in " + watch.getTime() / 1000 + " second.");
	}

	@Scheduled(cron = "0 0 2 * * ?")
	public void executeCnkiSpider() {
		StopWatch watch = new StopWatch();
		watch.start();

		cnkiSpider.run();

		watch.stop();
		log.info("Fetch CNKI data in " + watch.getTime() / 1000 + " second.");
	}
}
