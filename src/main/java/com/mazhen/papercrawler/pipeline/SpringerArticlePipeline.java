package com.mazhen.papercrawler.pipeline;

import com.mazhen.papercrawler.entity.SpringerArticleInfo;
import com.mazhen.papercrawler.repository.SpringerArticleRepository;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import javax.annotation.Resource;

/**
 * Created by smithma on 28/05/2017.
 */
@Component
public class SpringerArticlePipeline implements Pipeline {

	@Resource
	private SpringerArticleRepository springerArticleRepository;

	@Override
	public void process(ResultItems resultItems, Task task) {
		SpringerArticleInfo info = resultItems.get("info");
		if (info != null) {
			springerArticleRepository.saveAndFlush(info);
		}
	}
}
