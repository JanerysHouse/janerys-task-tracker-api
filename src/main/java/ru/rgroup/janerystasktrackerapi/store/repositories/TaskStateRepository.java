package ru.rgroup.janerystasktrackerapi.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskStateEntity;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}