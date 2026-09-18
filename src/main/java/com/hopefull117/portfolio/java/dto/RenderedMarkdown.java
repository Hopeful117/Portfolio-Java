package com.hopefull117.portfolio.java.dto;

import java.util.List;

public record RenderedMarkdown(String html, List<TableOfContentsEntry> tableOfContents) {
    public RenderedMarkdown {
        tableOfContents = List.copyOf(tableOfContents);
    }
}
