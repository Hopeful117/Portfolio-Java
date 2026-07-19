package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends MongoRepository<Article, String> {

    List<Article> findByPublishedTrue();


    Optional<Article> findBySlug(String slug);

    List<Article> findByPublishedTrueOrderByCreatedAtDesc();

}
