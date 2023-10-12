package ru.rgroup.janerystasktrackerapi.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rgroup.janerystasktrackerapi.store.entittes.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
