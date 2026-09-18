package com.hopefull117.portfolio.java.dto;

import com.hopefull117.portfolio.java.helper.TimelineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminTimelineRequest(@NotBlank String title, @NotNull LocalDate date, String description, String link, Integer displayOrder, @NotNull TimelineType type) {
}
