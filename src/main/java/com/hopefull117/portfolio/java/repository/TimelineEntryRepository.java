package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.TimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineEntryRepository extends JpaRepository<TimelineEntry,Long> {
    List<TimelineEntry> findAllByOrderByDisplayOrderAsc();
}
