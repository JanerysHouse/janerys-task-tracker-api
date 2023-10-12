package ru.rgroup.janerystasktrackerapi.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rgroup.janerystasktrackerapi.api.dto.ProjectDto;
import ru.rgroup.janerystasktrackerapi.api.exceptions.BadRequestException;
import ru.rgroup.janerystasktrackerapi.api.exceptions.NotFoundException;
import ru.rgroup.janerystasktrackerapi.api.factories.ProjectDtoFactory;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.repositories.ProjectRepository;

import java.util.Objects;


@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectDtoFactory projectDtoFactory;

    public static final String CREATE_PROJECT = "/api/project";
    public static final String EDIT_PROJECT = "/api/project/{project_id}";

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name) {

        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name cant be empty");
        }

        projectRepository.findByName(name).ifPresent(project -> {
            throw new BadRequestException(String.format("Project \"%s\" already exist.", name));
        });

        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name).
                        build()
        );

        return projectDtoFactory.makeProjectDto(project);
    }


    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editPatch(
            @PathVariable("project_id") Long projectId,
            @RequestParam String name) {

        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name cant be empty");
        }

        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with \"%s\" doesnt exist", projectId)));

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
            throw new BadRequestException(String.format("Project \"%s\" already exist.", name));
        });

        project.setName(name);
        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);

    }
}
