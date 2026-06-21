package com.hopefull117.portfolio.java.dto;

import com.hopefull117.portfolio.java.model.Technologies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private List<Technologies> technologies;

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String githubUrl;
}
