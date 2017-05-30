package com.mazhen.papercrawler.repository;

import com.mazhen.papercrawler.entity.SpringerArticleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by smithma on 28/05/2017.
 */
@Repository
public interface SpringerArticleRepository extends JpaRepository<SpringerArticleInfo, Long> {
}
