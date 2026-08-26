package com.hopefull117.portfolio.java.model;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article implements ModelEntity{
    @Id
    private String id;


    @NotBlank(message = "Le titre est obligatoire")
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
