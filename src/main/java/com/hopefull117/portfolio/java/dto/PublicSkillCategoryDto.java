package com.hopefull117.portfolio.java.dto;

import java.util.List;

public record PublicSkillCategoryDto(String category, List<PublicSkillDto> skills) {
    public PublicSkillCategoryDto {
        skills = List.copyOf(skills == null ? List.of() : skills);
    }
}
