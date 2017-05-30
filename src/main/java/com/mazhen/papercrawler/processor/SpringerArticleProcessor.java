package com.mazhen.papercrawler.processor;

import com.mazhen.papercrawler.entity.SpringerArticleInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;

/**
 * Created by smithma on 24/05/2017.
 */
@Slf4j
public class SpringerArticleProcessor implements PageProcessor {

	private static final String URL_JOURNAL = "(https://link\\.springer\\.com/journal/10694/[\\d\\-]+/[\\d\\-]+/page/[\\d\\-]+)";

	private static final String URL_ARTICLE = "(https://link\\.springer\\.com/article/[\\S\\-]+(?<!html)$)";

	private Site site = Site.me().setCycleRetryTimes(3).setTimeOut(5000);

	@Override
	public void process(Page page) {
		page.addTargetRequests(page.getHtml().links().regex(URL_JOURNAL).all());

		if (page.getUrl().regex(URL_JOURNAL).match()) {
			page.addTargetRequests(page.getHtml().links().regex(URL_ARTICLE).all());
		} else if (page.getUrl().regex(URL_ARTICLE).match()) {
			SpringerArticleInfo info = new SpringerArticleInfo();
			info.setJournalTitle(page.getHtml().xpath("//span[@class='JournalTitle']/html()").toString());
			info.setExtractDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
			info.setArticleTitle(page.getHtml().xpath("//h1[@class='ArticleTitle']/html()").toString());
			info.setArticleCitationYear(page.getHtml().xpath("//span[@class='ArticleCitation_Year']/time/@datetime").toString());
			info.setArticleCitationVolume(
				StringUtils.removeEnd(page.getHtml().xpath("//span[@class='ArticleCitation_Volume']/text(0)").toString(), ", "));
			info.setArticleCitationIssue(page.getHtml().xpath("//a[@class='ArticleCitation_Issue']/@title").toString());
			info.setArticleCitationPages(
				StringUtils.removeStart(page.getHtml().xpath("//span[@class='ArticleCitation_Pages']/text(0)").toString(), " "));
			info.setAuthors(StringUtils.join(page.getHtml().xpath("//span[@class='authors__name']/text(0)").all(), ","));
			info.setAffiliations(extractAffiliation(page.getHtml().xpath("//div[@class='content authors-affiliations u-interface']")));
			info.setFirstOnline(page.getHtml().xpath("//dd[@class='article-dates__first-online']/time/@datetime").toString());
			info.setCiteThis(page.getHtml().xpath("//dd[@id='citethis-text']/text(0)").toString());
			info.setDoi(StringUtils.removeStart(page.getHtml().xpath("//p[@class='article-doi']/text(0)").toString(), ": "));
			info.setCitations(page.getHtml().xpath("//span[@id='citations-count-number']/text(0)").toString());
			info.setDownloads(page.getHtml().xpath("//span[@class='article-metrics__views']/text(0)").toString());
			info.setSummary(page.getHtml().xpath("//section[@class='Abstract']/p[@class='Para']/text(0)").toString());
			info.setKeywords(StringUtils.join(page.getHtml().xpath("//span[@class='Keyword']/text(0)").all(), ","));
			info.setUrl(page.getUrl().toString());
		}
	}

	private String extractAffiliation(Selectable authorsandaffiliations) {
		Map<String, String> affiliationMap = extractAffiliationMap(authorsandaffiliations.xpath("//ol[@class='test-affiliations']"));
		Map<String, List<String>> authorMap = extractAuthorMap(authorsandaffiliations.xpath("//ul[@class='test-contributor-names']"), affiliationMap);

		return authorMap.toString();
	}

	private Map<String, List<String>> extractAuthorMap(Selectable names, Map<String, String> affiliationMap) {
		Map<String, List<String>> authorMap = new HashMap<>();

		for (Selectable name : names.xpath("//li").nodes()) {
			if (StringUtils.isBlank(name.xpath("/li/@data-affiliation").toString())) {
				String author = name.xpath("//span[@itemprop='name']/text(0)").toString();
				List<String> indexes = name.xpath("//ul[@data-role='AuthorsIndexes']/li/@data-affiliation").all();
				List<String> values = new ArrayList<>();
				for (String index : indexes) {
					values.add(affiliationMap.get(index));
				}
				authorMap.put(author, values);
			}
		}

		return authorMap;
	}

	private Map<String, String> extractAffiliationMap(Selectable affiliations) {
		Map<String, String> affiliationMap = new HashMap<>();

		for (Selectable affiliation : affiliations.xpath("//li").nodes()) {
			String dataTest = affiliation.xpath("/li/@data-test").toString();
			Selectable item = affiliation.xpath("//span[@class='affiliation__item']");
			String department = item.xpath("//span[@itemprop='department']/text(0)").toString();
			String name = item.xpath("//span[@itemprop='name']/text(0)").toString();
			String city = item.xpath("//span[@itemprop='city']/text(0)").toString();
			String country = item.xpath("//span[@itemprop='country']/text(0)").toString();
			affiliationMap.put(dataTest, department + ", " + name + ", " + city + ", " + country);
		}

		return affiliationMap;
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		StopWatch watch = new StopWatch();
		watch.start();

		Spider.create(new SpringerArticleProcessor()).addPipeline(new ConsolePipeline()).addUrl(
			"https://link.springer.com/journal/volumesAndIssues/10694").thread(5).run();

		watch.stop();
		log.info("Fetch data in " + watch.getTime() / 1000 + " second.");
	}
}
