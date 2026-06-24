package com.hopefull117.portfolio.java.dto;

import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.model.Skill;


import java.util.List;


public record  SkillCategoryDTO (Category category, List<Skill>skills){

}

