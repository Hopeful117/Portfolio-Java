package com.hopefull117.portfolio.java.dto;

import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.helper.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminSkillRequest(@NotBlank String name, @NotNull Category category, @NotNull SkillLevel skillLevel) {
}
