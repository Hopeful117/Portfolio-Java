package com.hopefull117.portfolio.java.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEditDTO {

    private Long id;

    private String title;
    private String description;
    private String githubUrl;

    private String imagePath;
    private MultipartFile image;

    private List<Long> technologyIds;
}
