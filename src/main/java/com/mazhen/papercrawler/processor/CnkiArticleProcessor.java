package com.mazhen.papercrawler.processor;

import com.mazhen.papercrawler.entity.CnkiArticleInfo;
import com.mazhen.papercrawler.util.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.Date;

/**
 * Created by smithma on 24/05/2017.
 */
@Slf4j
@Component
public class CnkiArticleProcessor implements PageProcessor {

	private static final String URL_JOURNAL = "(https://link\\.springer\\.com/journal/[\\d\\-]+/[\\d\\-]+/[\\d\\-]+/page/[\\d\\-]+)";

	private static final String URL_ARTICLE = "(https://link\\.springer\\.com/article/[\\S\\-]+(?<!html)$)";

	private Site site = Site.me().setCycleRetryTimes(3).setTimeOut(10000);

	//	@Override
	//	public void process(Page page) {
	//		page.addTargetRequests(page.getHtml().links().regex(URL_JOURNAL).all());
	//
	//		if (page.getUrl().regex(URL_JOURNAL).match()) {
	//			page.addTargetRequests(page.getHtml().links().regex(URL_ARTICLE).all());
	//		} else if (page.getUrl().regex(URL_ARTICLE).match()) {
	//			CnkiArticleInfo info = getCnkiArticleInfo(page);
	//			page.putField("info", info);
	//		}
	//	}

	@Override
	public void process(Page page) {
		CnkiArticleInfo info = getCnkiArticleInfo(page);
		log.info("info: " + info.toString());
	}

	private CnkiArticleInfo getCnkiArticleInfo(Page page) {
		CnkiArticleInfo info = new CnkiArticleInfo();
		info.setExtractDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));

		processTitleDiv(page.getHtml().xpath("//div[@class='wxTitle']"), info);
		processBaseInfoDiv(page.getHtml().xpath("//div[@class='wxBaseinfo']"), info);
		processSourInfoDiv(page.getHtml().xpath("//div[@class='sourinfo']"), info);

		info.setCitations(DataUtils.removeBracket(
			page.getHtml().xpath("//div[@class='MapAreaLeft']/div[@class='map']/div[@class='yzwx']/span/text(0)").toString()));
		info.setUrl(page.getUrl().toString());

		return info;
	}

	private void processTitleDiv(Selectable titleDiv, CnkiArticleInfo info) {
		info.setArticleTitle(titleDiv.xpath("//h2[@class='title']/text(0)").toString());
		info.setAuthors(DataUtils.transformNodeList(titleDiv.xpath("//div[@class='author']/span/a/text(0)")));
		info.setAffiliations(DataUtils.transformNodeList(titleDiv.xpath("//div[@class='orgn']/span/a/text(0)")));
	}

	private void processBaseInfoDiv(Selectable baseInfoDiv, CnkiArticleInfo info) {

		for (Selectable node : baseInfoDiv.xpath("//p").nodes()) {
			String id = node.xpath("/p/label/@id").toString();
			if ("catalog_ABSTRACT".equals(id)) {
				info.setSummary(node.xpath("//span[@id='ChDivSummary']/text(0)").toString());
			} else if ("catalog_FUND".equals(id)) {
				info.setFund(DataUtils.transformNodeList(node.xpath("/p/a/text(0)"), "； "));
			} else if ("catalog_KEYWORD".equals(id)) {
				info.setKeywords(DataUtils.transformNodeList(node.xpath("/p/a/text(0)"), "; "));
			} else if ("catalog_ZTCLS".equals(id)) {
				info.setCategory(node.xpath("/p/text(0)").toString());
			}
		}

		info.setDownloads(baseInfoDiv.xpath("//div[@class='total']/span[@class='a']/b/text(0)").toString());
	}

	private void processSourInfoDiv(Selectable sourDiv, CnkiArticleInfo info) {
		info.setJournalTitle(sourDiv.xpath("//p[@class='title']/a/text(0)").toString());
		info.setArticleCitationYear(sourDiv.xpath("//p[3]/a/text(0)").toString());
	}

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		Spider.create(new CnkiArticleProcessor()).addPipeline(new ConsolePipeline()).addUrl(
			"http://kns.cnki.net/kcms/detail/detail.aspx?dbcode=CJFD&filename=XFKJ201702002&dbname=CJFDTEMP").run();
	}
}
