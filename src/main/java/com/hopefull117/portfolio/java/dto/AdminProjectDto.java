package com.hopefull117.portfolio.java.dto;

import java.util.List;

public record AdminProjectDto(Long id, String title, String description, String githubUrl, String imageUrl, List<AdminTechnologyDto> technologies) {
}
