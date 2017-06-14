package com.mazhen.papercrawler.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by smithma on 03/06/2017.
 */
@Data
@Entity
@Table(name = "cnki_article_info",
	uniqueConstraints = @UniqueConstraint(columnNames = { "articleTitle", "extractDate" }),
	indexes = {
		@Index(name = "cnki_article_info_idx01", columnList = "articleTitle"),
		@Index(name = "cnki_article_info_idx02", columnList = "extractDate")
	})
public class CnkiArticleInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "journalTitle", columnDefinition = "varchar(255)", nullable = false)
	private String journalTitle;

	@Column(name = "extractDate", columnDefinition = "varchar(20)", nullable = false)
	private String extractDate;

	@Column(name = "articleTitle", columnDefinition = "varchar(255)", nullable = false)
	private String articleTitle;

	@Column(name = "articleCitationYear", columnDefinition = "varchar(20)")
	private String articleCitationYear;

	@Column(name = "authors", columnDefinition = "varchar(255)")
	private String authors;

	@Column(name = "affiliations", columnDefinition = "varchar(3000)")
	private String affiliations;

	@Column(name = "abstract", columnDefinition = "varchar(10000)")
	private String summary;

	@Column(name = "keywords", columnDefinition = "varchar(1000)")
	private String keywords;

	@Column(name = "fund", columnDefinition = "varchar(3000)")
	private String fund;

	@Column(name = "category", columnDefinition = "varchar(255)")
	private String category;

	@Column(name = "citations", columnDefinition = "varchar(10)")
	private String citations;

	@Column(name = "downloads", columnDefinition = "varchar(10)")
	private String downloads;

	@Column(name = "url", columnDefinition = "varchar(255)")
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdAt", nullable = false, updatable = false)
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifiedAt", nullable = false)
	private Date modifiedAt;

	@PrePersist
	void onCreate() {
		Date now = new Date();
		createdAt = now;
		modifiedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		modifiedAt = new Date();
	}
}
