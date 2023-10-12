package ru.rgroup.janerystasktrackerapi.api.factories;

import org.springframework.stereotype.Component;
import ru.rgroup.janerystasktrackerapi.api.dto.ProjectDto;
import ru.rgroup.janerystasktrackerapi.api.dto.TaskStateDto;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;

@Component
public class TaskStateDtoFactory {

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {

        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createAt(entity.getCreateAt())
                .ordinal(entity.getOrdinal())
                .build();
    }

}
