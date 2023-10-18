package ru.rgroup.janerystasktrackerapi.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rgroup.janerystasktrackerapi.api.controllers.helpers.ControllerHelper;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskStateDto;
import ru.rgroup.janerystasktrackerapi.api.exceptions.BadRequestException;
import ru.rgroup.janerystasktrackerapi.api.factories.TaskStateDtoFactory;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;
import ru.rgroup.janerystasktrackerapi.store.repositories.TaskStateRepository;

import java.util.List;

import java.util.stream.Collectors;


@RequiredArgsConstructor
@Transactional
@RestController
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;
    private final TaskStateDtoFactory taskStateDtoFactory;
    private final ControllerHelper controllerHelper;


    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";


    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId) {

       ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(@PathVariable(name = "project_id") Long projectId,
                                        @RequestParam(name = "task_state_name") String taskStateName) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name cant be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);
        project.getTaskStates()
                .stream()
                .map(TaskStateEntity::getName)
                .filter(anotherTaskStateName -> anotherTaskStateName.equalsIgnoreCase(taskStateName))
                .findAny()
                .ifPresent(it -> {
                    throw new BadRequestException(String.format("Task state \"%s\" already exist.", taskStateName));
                });


    }


}
