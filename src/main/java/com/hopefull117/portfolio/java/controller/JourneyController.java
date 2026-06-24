package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.service.TimelineEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class JourneyController {
    private final TimelineEntryService timelineEntryService;
    @GetMapping("/journey")
    public String journey(Model model) {
        log.info("Accès page journey");
        model.addAttribute("title","Journey");
        model.addAttribute("timelineEntries",timelineEntryService.getTimeline());
        return "journey";
    }
}
