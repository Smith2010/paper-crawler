package com.mazhen.papercrawler.processor;

import com.alibaba.fastjson.JSONObject;
import com.mazhen.papercrawler.entity.SpringerArticleInfo;
import com.mazhen.papercrawler.util.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
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
@Component
public class SpringerArticleProcessor implements PageProcessor {

	private static final String URL_JOURNAL = "(https://link\\.springer\\.com/journal/[\\d\\-]+/[\\d\\-]+/[\\d\\-]+/page/[\\d\\-]+)";

	private static final String URL_ARTICLE = "(https://link\\.springer\\.com/article/[\\S\\-]+(?<!html)$)";

	private Site site = Site.me().setCycleRetryTimes(3).setTimeOut(10000);

	@Override
	public void process(Page page) {
		page.addTargetRequests(page.getHtml().links().regex(URL_JOURNAL).all());

		if (page.getUrl().regex(URL_JOURNAL).match()) {
			page.addTargetRequests(page.getHtml().links().regex(URL_ARTICLE).all());
		} else if (page.getUrl().regex(URL_ARTICLE).match()) {
			SpringerArticleInfo info = getSpringerArticleInfo(page);
			page.putField("info", info);
		}
	}

	private SpringerArticleInfo getSpringerArticleInfo(Page page) {
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
		info.setAuthors(DataUtils.transformNodeList(page.getHtml().xpath("//span[@class='authors__name']/text(0)")));
		info.setAffiliations(extractAffiliations(page.getHtml().xpath("//div[@class='content authors-affiliations u-interface']")));

		info.setFirstOnline(page.getHtml().xpath("//dd[@class='article-dates__first-online']/time/@datetime").toString());
		info.setCiteThis(page.getHtml().xpath("//dd[@id='citethis-text']/text(0)").toString());
		info.setDoi(StringUtils.removeStart(page.getHtml().xpath("//p[@class='article-doi']/text(0)").toString(), ": "));
		info.setCitations(DataUtils.transformNumber(page.getHtml().xpath("//span[@id='citations-count-number']/text(0)").toString()));
		info.setDownloads(DataUtils.transformNumber(page.getHtml().xpath("//span[@class='article-metrics__views']/text(0)").toString()));
		info.setSummary(page.getHtml().xpath("//section[@class='Abstract']/p[@class='Para']/text(0)").toString());
		info.setKeywords(extractKeywords(page.getHtml().xpath("//span[@class='Keyword']/html()")));
		info.setUrl(page.getUrl().toString());
		return info;
	}

	private String extractAffiliations(Selectable authorsAndAffiliations) {
		Map<String, String> affiliationMap = extractAffiliationMap(authorsAndAffiliations.xpath("//ol[@class='test-affiliations']"));

		if (affiliationMap.isEmpty()) {
			return null;
		}

		Map<String, List<String>> authorMap = extractAuthorMap(authorsAndAffiliations.xpath("//ul[@class='test-contributor-names']"), affiliationMap);

		JSONObject json = new JSONObject();
		json.putAll(authorMap);

		return json.toString();
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

			List<String> result = new ArrayList<>();
			if (department != null) {
				result.add(department);
			}

			if (name != null) {
				result.add(name);
			}

			if (city != null) {
				result.add(city);
			}

			if (country != null) {
				result.add(country);
			}

			affiliationMap.put(dataTest, StringUtils.join(result,","));
		}

		return affiliationMap;
	}

	private String extractKeywords(Selectable keywords) {
		List<String> list = new ArrayList<>();
		for (Selectable keyword : keywords.nodes()) {
			list.add(StringUtils.removeEnd(keyword.toString(), "&nbsp;"));
		}

		return list.isEmpty() ? null : StringUtils.join(list, ",");
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		Spider.create(new SpringerArticleProcessor()).addPipeline(new ConsolePipeline()).addUrl(
			"https://link.springer.com/article/10.1007/s10694-016-0618-y").run();
	}
}
