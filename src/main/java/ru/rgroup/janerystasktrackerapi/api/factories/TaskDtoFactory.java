package ru.rgroup.janerystasktrackerapi.api.factories;

import org.springframework.stereotype.Component;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskDto;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskEntity;

@Component
public class TaskDtoFactory {

    public TaskDto makeTaskDto(TaskEntity entity) {

        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

}
