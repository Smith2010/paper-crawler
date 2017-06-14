package com.mazhen.papercrawler.configuration;

import com.mazhen.papercrawler.pipeline.CnkiArticlePipeline;
import com.mazhen.papercrawler.pipeline.SpringerArticlePipeline;
import com.mazhen.papercrawler.processor.CnkiArticleProcessor;
import com.mazhen.papercrawler.processor.SpringerArticleProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

/**
 * Created by smithma on 10/06/2017.
 */
@Configuration
public class SpiderConfiguration {

	@Bean(name="springerSpider")
	public Spider SpringerSpider(@Qualifier("springerArticleProcessor") SpringerArticleProcessor springerArticleProcessor,
		@Qualifier("springerArticlePipeline") SpringerArticlePipeline springerArticlePipeline) {
		return Spider.create(springerArticleProcessor)
			.addPipeline(springerArticlePipeline)
			.addPipeline(new ConsolePipeline())
			.addUrl("https://link.springer.com/journal/volumesAndIssues/10694")
			.addUrl("https://link.springer.com/journal/volumesAndIssues/40038")
			.thread(5);
	}

	@Bean(name="cnkiSpider")
	public Spider CnkiSpider(@Qualifier("cnkiArticleProcessor") CnkiArticleProcessor cnkiArticleProcessor,
		@Qualifier("cnkiArticlePipeline") CnkiArticlePipeline cnkiArticlePipeline) {
		return Spider.create(cnkiArticleProcessor)
			.addPipeline(cnkiArticlePipeline)
			.addPipeline(new ConsolePipeline())
			.addUrl(CnkiArticleProcessor.URL_JOURNAL)
			.thread(5);
	}
}