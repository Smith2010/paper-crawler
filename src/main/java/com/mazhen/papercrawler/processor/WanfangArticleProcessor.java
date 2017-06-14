package com.mazhen.papercrawler.processor;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smithma on 24/05/2017.
 */
@Slf4j
@Component
public class WanfangArticleProcessor implements PageProcessor {

	public static final String URL_ARTICLE_PREFIX = "http://d.wanfangdata.com.cn/Periodical/xfkxyjs";

	private Site site = Site.me().setCharset("UTF-8").setCycleRetryTimes(3).setTimeOut(10000);

	@Override
	public void process(Page page) {
		String affiliations = extractAffiliations(page.getHtml().xpath("//div[@class='fixed-width baseinfo-feild']"));

		page.putField("affiliations", affiliations);
	}

	private String extractAffiliations(Selectable baseInfos) {
		List<String> affiliationList = new ArrayList<>();
		Map<String, String> authorMap = new HashMap<>();

		for (Selectable info : baseInfos.xpath("//div[@class='row']").nodes()) {
			String pre = info.xpath("//span[@class='pre']/text(0)").toString();
			if (StringUtils.equals(pre, "作者单位：")) {
				List<Selectable> nodes = info.xpath("//span[@class='text']/span").nodes();
				if (nodes.isEmpty()) {
					affiliationList.add(StringUtils.trim(info.xpath("//span[@class='text']/text(0)").toString()));
				} else {
					affiliationList.addAll(info.xpath("//span[@class='text']/span/text(0)").all());
				}
			} else if (StringUtils.equals(pre, "作者：")) {
				List<String> names = info.xpath("//span[@class='text']/a/text(0)").all();

				for (int i = 0; i < names.size(); i++) {
					authorMap.put(names.get(i), info.xpath("//span[@class='text']/sup/text(0)").toString());
				}
			}
		}

		if (authorMap.isEmpty()) {
			return null;
		}
//
//		Map<String, List<String>> authorMap = extractAuthorMap(baseInfo.xpath("//ul[@class='test-contributor-names']"), affiliationMap);

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

	@Override
	public Site getSite() {
		return site;
	}

	public static void main(String[] args) {
		Spider.create(new WanfangArticleProcessor()).addPipeline(new ConsolePipeline())
			.addUrl("http://d.wanfangdata.com.cn/Periodical/xfkxyjs201703001")
			.run();
	}
}
