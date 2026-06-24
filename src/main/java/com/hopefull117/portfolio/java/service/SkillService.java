package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.SkillCategoryDTO;
import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkillService extends AbstractCrudService<Skill> {

    protected SkillService(JpaRepository<Skill, Long> jpaRepository) {
        super(jpaRepository);
    }

    public List<SkillCategoryDTO> getSkillsGroupedByCategory() {
        Map<Category, List<Skill>> groupedSkills = jpaRepository.findAll().stream().collect(Collectors.groupingBy(Skill::getCategory, Collectors.mapping(skill -> skill, Collectors.toList())

        ));
        return groupedSkills.entrySet()
                .stream()
                .map(entry -> new SkillCategoryDTO(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    }

