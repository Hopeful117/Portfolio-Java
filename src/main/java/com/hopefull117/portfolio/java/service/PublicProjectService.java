package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.PublicProjectSummaryDto;
import com.hopefull117.portfolio.java.dto.PublicTechnologyDto;
import com.hopefull117.portfolio.java.model.Project;
import com.hopefull117.portfolio.java.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProjectService {
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<PublicProjectSummaryDto> findPublicSummaries() {
        return projectRepository.findAll().stream()
                .map(this::toPublicSummary)
                .toList();
    }

    private PublicProjectSummaryDto toPublicSummary(Project project) {
        List<PublicTechnologyDto> technologies = project.getTechnologies() == null
                ? List.of()
                : project.getTechnologies().stream()
                .map(technology -> new PublicTechnologyDto(technology.getName(), technology.getIconeUrl()))
                .toList();

        return new PublicProjectSummaryDto(
                project.getTitle(),
                project.getDescription(),
                project.getImagePath(),
                project.getGithubUrl(),
                technologies
        );
    }
}
