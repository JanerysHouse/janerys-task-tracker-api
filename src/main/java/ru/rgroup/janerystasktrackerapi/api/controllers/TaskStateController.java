package ru.rgroup.janerystasktrackerapi.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rgroup.janerystasktrackerapi.api.controllers.helpers.ControllerHelper;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskStateDto;
import ru.rgroup.janerystasktrackerapi.api.exceptions.BadRequestException;
import ru.rgroup.janerystasktrackerapi.api.exceptions.NotFoundException;
import ru.rgroup.janerystasktrackerapi.api.factories.TaskStateDtoFactory;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;
import ru.rgroup.janerystasktrackerapi.store.repositories.TaskStateRepository;

import java.util.List;

import java.util.Optional;
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
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";


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

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState : project.getTaskStates()) {
            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));
            }
            if (taskState.getRightTaskState().isEmpty()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }

        }

        TaskStateEntity taskState = taskStateRepository
                .saveAndFlush(TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(project)
                        .build());

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);


    }

    @PostMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(@PathVariable(name = "task_state_id") Long taskStateId,
                                        @RequestParam(name = "task_state_name") String taskStateName) {
        if (taskStateName.isBlank()) {
            throw new BadRequestException("Task state name cant be empty");
        }

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);
        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state \"%s\" already exist.", taskStateName));
                });

        taskState.setName(taskStateName);
        taskState = taskStateRepository.saveAndFlush(taskState);
        return taskStateDtoFactory.makeTaskStateDto(taskState);


    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException
                        (String.format("Task state with \"%s\" id doesnt exist.", taskStateId)));
    }

}


