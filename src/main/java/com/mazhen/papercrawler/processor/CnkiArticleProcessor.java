package com.mazhen.papercrawler.processor;

import com.mazhen.papercrawler.entity.CnkiArticleInfo;
import com.mazhen.papercrawler.util.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by smithma on 24/05/2017.
 */
@Slf4j
@Component
public class CnkiArticleProcessor implements PageProcessor {

	public static final String URL_JOURNAL = "http://navi.cnki.net/KNavi/JournalDetail?pcode=CJFD&pykm=XFKJ";

	public static final String URL_ARTICLE_LIST_PREFIX = "http://navi.cnki.net/knavi/JournalDetail/GetArticleList";

	private static final String URL_ARTICLE_PREFIX = "http://kns.cnki.net/kcms/detail/detail.aspx?";

	private Site site = Site.me().setCharset("UTF-8").setCycleRetryTimes(3).setTimeOut(10000).setSleepTime(2000);

	@Override
	public void process(Page page) {
		if (URL_JOURNAL.equals(page.getRequest().getUrl())) {
			addArticleListUrls(page);
		} else if (StringUtils.startsWith(page.getUrl().toString(), URL_ARTICLE_LIST_PREFIX)) {
			addArticleUrls(page);
		} else if (StringUtils.startsWith(page.getUrl().toString(), URL_ARTICLE_PREFIX)) {
			CnkiArticleInfo info = getCnkiArticleInfo(page);
			page.putField("info", info);
		}
	}

	private void addArticleListUrls(Page page) {
		try (SeleniumDownloader downloader = new SeleniumDownloader("/papercrawler/driver/chromedriver")) {
			Spider menuSpider = Spider.create(new CnkiMenuProcessor()).setDownloader(downloader.setSleepTime(5000));
			ResultItems result = menuSpider.get(URL_JOURNAL);
			List<String> articleListUrls = result.get("articleListUrls");
			for (String url : articleListUrls) {
				page.addTargetRequest(url);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void addArticleUrls(Page page) {
		List<String> articleUrls = page.getHtml().xpath("//span[@class='name']").links().all();
		for (String articleUrl : articleUrls) {
			String params = articleUrl.substring(articleUrl.indexOf('&'));
			String url = URL_ARTICLE_PREFIX + params;
			page.addTargetRequest(url);
		}
	}

	private CnkiArticleInfo getCnkiArticleInfo(Page page) {
		CnkiArticleInfo info = new CnkiArticleInfo();
		info.setExtractDate(DateFormatUtils.format(new Date(), "yyyyMMdd"));
		info.setUrl(page.getUrl().toString());

		processTitleDiv(page.getHtml().xpath("//div[@class='wxTitle']"), info);
		processBaseInfoDiv(page.getHtml().xpath("//div[@class='wxBaseinfo']"), info);
		processSourInfoDiv(page.getHtml().xpath("//div[@class='sourinfo']"), info);

		info.setCitations(DataUtils.removeBracket(
			page.getHtml().xpath("//div[@class='MapAreaLeft']/div[@class='map']/div[@class='yzwx']/span/text(0)").toString()));

		return info;
	}

	private void processTitleDiv(Selectable titleDiv, CnkiArticleInfo info) {
		info.setArticleTitle(titleDiv.xpath("//h2[@class='title']/text(0)").toString());
		info.setAuthors(DataUtils.transformNodeList(titleDiv.xpath("//div[@class='author']/span/a/text(0)"), ","));

		String filename = DataUtils.getCnkiUrlFilename(info.getUrl());
		if (Integer.valueOf(filename) > 200004036) {
			String url = WanfangArticleProcessor.URL_ARTICLE_PREFIX + filename;
			Spider wanfangSpider = Spider.create(new WanfangArticleProcessor());
			ResultItems result = wanfangSpider.get(url);
			if (result != null && StringUtils.isNotBlank(result.get("affiliations"))) {
				info.setAffiliations(result.get("affiliations"));
				wanfangSpider.close();
			} else {
				info.setAffiliations(DataUtils.transformNodeList(titleDiv.xpath("//div[@class='orgn']/span/a/text(0)"), ";"));
			}
		} else {
			info.setAffiliations(DataUtils.transformNodeList(titleDiv.xpath("//div[@class='orgn']/span/a/text(0)"), ";"));
		}

	}

	private void processBaseInfoDiv(Selectable baseInfoDiv, CnkiArticleInfo info) {

		for (Selectable node : baseInfoDiv.xpath("//p").nodes()) {
			String id = node.xpath("/p/label/@id").toString();
			if ("catalog_ABSTRACT".equals(id)) {
				info.setSummary(node.xpath("//span[@id='ChDivSummary']/text(0)").toString());
			} else if ("catalog_FUND".equals(id)) {
				info.setFund(DataUtils.transformNodeList(node.xpath("/p/a/text(0)"), "； ", ","));
			} else if ("catalog_KEYWORD".equals(id)) {
				info.setKeywords(DataUtils.transformNodeList(node.xpath("/p/a/text(0)"), "; ", ","));
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
}
