package com.hopefull117.portfolio.java.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AdminProjectRequest(@NotBlank String title, @NotBlank String description, @NotBlank String githubUrl, List<Long> technologyIds, Boolean featured) {
}
