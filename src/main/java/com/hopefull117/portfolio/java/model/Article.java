package com.hopefull117.portfolio.java.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article implements ModelEntity{
    @Id
    private String id;


    private String title;


    private String slug;


    private String excerpt;


    private String content;


    private String coverImage;


    private List<String> tags;


    private boolean published;


    private Instant createdAt;


    private Instant updatedAt;
}
