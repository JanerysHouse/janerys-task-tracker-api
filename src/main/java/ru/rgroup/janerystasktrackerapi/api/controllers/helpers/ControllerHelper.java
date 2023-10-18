package ru.rgroup.janerystasktrackerapi.api.controllers.helpers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ru.rgroup.janerystasktrackerapi.api.exceptions.NotFoundException;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.repositories.ProjectRepository;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {
    private final ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with \"%s\" doesnt exist", projectId)));
    }
}
