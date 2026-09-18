package com.hopefull117.portfolio.java.dto;

public record AdminSessionDto(boolean authenticated, String username, String role) {
}
