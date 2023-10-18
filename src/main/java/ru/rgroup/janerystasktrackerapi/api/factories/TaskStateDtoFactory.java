package ru.rgroup.janerystasktrackerapi.api.factories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskStateDto;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TaskStateDtoFactory {

    private final TaskDtoFactory taskDtoFactory;

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {

        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createAt(entity.getCreateAt())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .tasks(entity.getTask()
                        .stream()
                        .map(taskDtoFactory::makeTaskDto)
                        .collect(Collectors.toList()))
                .build();
    }

}
