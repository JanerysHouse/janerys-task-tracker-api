package ru.rgroup.janerystasktrackerapi.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.rgroup.janerystasktrackerapi.api.controllers.helpers.ControllerHelper;
import ru.rgroup.janerystasktrackerapi.api.dto.AskDto;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskStateDto;
import ru.rgroup.janerystasktrackerapi.api.exceptions.BadRequestException;
import ru.rgroup.janerystasktrackerapi.api.exceptions.NotFoundException;
import ru.rgroup.janerystasktrackerapi.api.factories.TaskStateDtoFactory;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;
import ru.rgroup.janerystasktrackerapi.store.repositories.TaskStateRepository;

import java.util.List;

import java.util.Objects;
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
    public static final String CHANGE_TASK_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}";


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

    @PatchMapping(UPDATE_TASK_STATE)
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

    @PatchMapping(CHANGE_TASK_POSITION)
    public TaskStateDto changeTaskPosition(@PathVariable(name = "task_state_id") Long taskStateId,
                                        @RequestParam(name = "left_task_state_id", required = false)
                                        Optional<Long> optionalLeftTaskStateId) {


        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);
        ProjectEntity project = changeTaskState.getProject();

        Optional<Long> oldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if (oldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
        }


        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {
            if (taskStateId.equals(leftTaskStateId)) {
                throw new BadRequestException("Lest task state id equals changed task state");
            }
            TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);
            if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                throw new BadRequestException("Task state position can be changed within the same project");
            }

            return leftTaskStateEntity;
        });

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if (optionalNewLeftTaskState.isEmpty()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
                    .findAny();

        } else {
            optionalNewRightTaskState = optionalNewLeftTaskState.get().getRightTaskState();
        }

        replaceOldTaskStatePosition(changeTaskState);

        if (optionalNewRightTaskState.isPresent()) {

            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();
            newRightTaskState.setRightTaskState(changeTaskState);
            changeTaskState.setRightTaskState(newRightTaskState);

        } else {
            changeTaskState.setLeftTaskState(null);
        }
        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);
        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);
        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoFactory.makeTaskStateDto(changeTaskState);


    }
    @DeleteMapping(DELETE_TASK_STATE)
    public AskDto deleteTaskState(@PathVariable(name = "task_state_id") Long taskStateId) {

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(changeTaskState);

        taskStateRepository.delete(changeTaskState);

        return AskDto.builder().answer(true).build();


    }

    private void replaceOldTaskStatePosition(TaskStateEntity changeTaskState) {
        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState.ifPresent(it -> {
            it.setRightTaskState(optionalOldRightTaskState.orElse(null));
            taskStateRepository.saveAndFlush(it);
        });
        optionalOldRightTaskState.ifPresent(it -> {
            it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
            taskStateRepository.saveAndFlush(it);
        });
    }


    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException
                        (String.format("Task state with \"%s\" id doesnt exist.", taskStateId)));
    }

}


