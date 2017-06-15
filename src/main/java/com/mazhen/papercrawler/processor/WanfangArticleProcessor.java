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
		String authorAffiliationsMapping = extractAuthorAffiliationsMapping(page.getHtml().xpath("//div[@class='fixed-width baseinfo-feild']"));
		page.putField("affiliations", authorAffiliationsMapping);
	}

	private String extractAuthorAffiliationsMapping(Selectable baseInfos) {
		List<String> affiliationList = extractAffiliationList(baseInfos);
		Map<String, String> authorMap = extractAuthorMap(baseInfos, affiliationList);

		if (authorMap.isEmpty()) {
			return null;
		}

		JSONObject json = new JSONObject();
		json.putAll(authorMap);

		return json.toString();
	}

	private Map<String, String> extractAuthorMap(Selectable baseInfos, List<String> affiliationList) {
		Map<String, String> authorMap = new HashMap<>();

		for (Selectable info : baseInfos.xpath("//div[@class='row']").nodes()) {
			String pre = info.xpath("//span[@class='pre']/text(0)").toString();
			if (StringUtils.equals(pre, "作者：")) {
				List<String> names = info.xpath("//span[@class='text']/a/text(0)").all();

				for (int i = 0; i < names.size(); i++) {
					String supIndex = String.valueOf(i + 1);
					String indexStr = StringUtils.substring(info.xpath("//span[@class='text']/sup[" + supIndex + "]/text(0)").toString(), 1, 2);
					int index = StringUtils.isBlank(indexStr) ? 0 : Integer.valueOf(indexStr) - 1;
					authorMap.put(names.get(i), affiliationList.get(index));
				}
			}
		}
		return authorMap;
	}

	private List<String> extractAffiliationList(Selectable baseInfos) {
		List<String> affiliationList = new ArrayList<>();

		for (Selectable info : baseInfos.xpath("//div[@class='row']").nodes()) {
			String pre = info.xpath("//span[@class='pre']/text(0)").toString();
			if (StringUtils.equals(pre, "作者单位：")) {
				List<Selectable> nodes = info.xpath("//span[@class='text']/span").nodes();
				if (nodes.isEmpty()) {
					affiliationList.add(StringUtils.trim(info.xpath("//span[@class='text']/text(0)").toString()));
				} else {
					affiliationList.addAll(info.xpath("//span[@class='text']/span/text(0)").all());
				}
				break;
			}
		}

		return affiliationList;
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
