package com.hopefull117.portfolio.java.dto;


import com.hopefull117.portfolio.java.model.Technology;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsDTO {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private List<Long> technologies;

    private MultipartFile image;

    @NotBlank
    private String githubUrl;
}
