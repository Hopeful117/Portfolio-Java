package com.hopefull117.portfolio.java.dto;

import java.time.LocalDate;

public record AdminTimelineDto(Long id, String title, LocalDate date, String description, String link, Integer displayOrder, String type) {
}
