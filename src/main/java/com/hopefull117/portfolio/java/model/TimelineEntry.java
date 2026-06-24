package com.hopefull117.portfolio.java.model;

import com.hopefull117.portfolio.java.helper.TimelineType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "timeline_entries")
public class TimelineEntry implements ModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String title;

    private LocalDate date;

    @Column(length = 2000)
    private String description;

    private String link;

    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    private TimelineType type;
}
