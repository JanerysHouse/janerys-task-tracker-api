package ru.rgroup.janerystasktrackerapi.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;

import java.util.Optional;


public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {

    Optional<TaskStateEntity> findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(Long projectId, String taskStateName);

}
