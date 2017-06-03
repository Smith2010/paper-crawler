package com.mazhen.papercrawler.pipeline;

import com.mazhen.papercrawler.entity.CnkiArticleInfo;
import com.mazhen.papercrawler.repository.CnkiArticleRepository;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import javax.annotation.Resource;

/**
 * Created by smithma on 28/05/2017.
 */
@Component
public class CnkiArticlePipeline implements Pipeline {

	@Resource
	private CnkiArticleRepository cnkiArticleRepository;

	@Override
	public void process(ResultItems resultItems, Task task) {
		CnkiArticleInfo info = resultItems.get("info");
		if (info != null) {
			cnkiArticleRepository.saveAndFlush(info);
		}
	}
}
