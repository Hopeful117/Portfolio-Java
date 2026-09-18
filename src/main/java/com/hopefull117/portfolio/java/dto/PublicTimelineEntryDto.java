package com.hopefull117.portfolio.java.dto;

import java.time.LocalDate;

public record PublicTimelineEntryDto(
        String title,
        LocalDate date,
        String description,
        String link,
        String type
) {
}
