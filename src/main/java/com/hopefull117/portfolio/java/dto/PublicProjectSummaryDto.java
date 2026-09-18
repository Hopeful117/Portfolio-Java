package com.hopefull117.portfolio.java.dto;

import java.util.List;

public record PublicProjectSummaryDto(
        String title,
        String description,
        String imageUrl,
        String repositoryUrl,
        boolean featured,
        List<PublicTechnologyDto> technologies
) {
    public PublicProjectSummaryDto {
        technologies = List.copyOf(technologies == null ? List.of() : technologies);
    }
}
