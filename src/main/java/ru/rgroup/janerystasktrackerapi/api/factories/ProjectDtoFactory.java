package ru.rgroup.janerystasktrackerapi.api.factories;

import org.springframework.stereotype.Component;
import ru.rgroup.janerystasktrackerapi.api.dto.ProjectDto;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;

@Component
public class ProjectDtoFactory {

    public ProjectDto makeProjectDto(ProjectEntity entity) {

        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createAt(entity.getCreateAt())
                .build();
    }

}
