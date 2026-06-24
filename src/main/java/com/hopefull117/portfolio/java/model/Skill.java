package com.hopefull117.portfolio.java.model;

import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.helper.SkillLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Level;

@Entity
@Getter
@Setter
@Table(name="skill")
public class Skill implements ModelEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Enumerated(EnumType.STRING)
    Category category;

    @Enumerated(EnumType.STRING)
    SkillLevel skillLevel;

}
