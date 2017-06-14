package com.mazhen.papercrawler.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smithma on 24/05/2017.
 */
@Slf4j
@Component
public class CnkiMenuProcessor implements PageProcessor {

	private Site site = Site.me().setCycleRetryTimes(3).setTimeOut(10000);

	@Override
	public void process(Page page) {
		List<String> articleListUrls = new ArrayList<>();

		for (String yearIssue : page.getHtml().xpath("//div[@class='yearissuepage']/dl/dd/a/@id").all()) {
			String year = yearIssue.substring(2, 6);
			String issue = yearIssue.substring(6, 8);
			String url = CnkiArticleProcessor.URL_ARTICLE_LIST_PREFIX + "?year=" + year + "&issue=" + issue + "&pykm=XFKJ&pageIdx=0";
			articleListUrls.add(url);
		}

		page.putField("articleListUrls", articleListUrls);
	}

	@Override
	public Site getSite() {
		return site;
	}
}
