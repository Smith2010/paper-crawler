package com.mazhen.papercrawler.service;

import com.mazhen.papercrawler.pipeline.SpringerArticlePipeline;
import com.mazhen.papercrawler.processor.SpringerArticleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
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
	private SpringerArticleProcessor springerArticleProcessor;

	@Autowired
	private SpringerArticlePipeline springerArticlePipeline;


	@Scheduled(fixedRate = 500000000)
	public void execute() {
		StopWatch watch = new StopWatch();
		watch.start();

		Spider.create(springerArticleProcessor)
			.addPipeline(springerArticlePipeline)
			.addUrl("https://link.springer.com/journal/volumesAndIssues/10694")
			.addUrl("https://link.springer.com/journal/volumesAndIssues/40038")
//			.addUrl("https://link.springer.com/article/10.1007/s10694-016-0618-y")
			.thread(10)
			.run();

		watch.stop();
		log.info("Fetch data in " + watch.getTime() / 1000 + " second.");
	}
}
