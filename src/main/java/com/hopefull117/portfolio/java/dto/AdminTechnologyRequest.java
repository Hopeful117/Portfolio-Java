package com.hopefull117.portfolio.java.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminTechnologyRequest(@NotBlank String name, String iconUrl) {
}
