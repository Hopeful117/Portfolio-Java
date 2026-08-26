package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends MongoRepository<Article, String> {

    List<Article> findByPublishedTrue();


    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Article> findByPublishedTrueOrderByCreatedAtDesc();

}
