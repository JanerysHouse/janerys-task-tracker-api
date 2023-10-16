package ru.rgroup.janerystasktrackerapi.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rgroup.janerystasktrackerapi.store.entittes.ProjectEntity;

import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findByName(String name);

    Stream<ProjectEntity> streamAll();
    Stream<ProjectEntity> streamAllByNameStartsWithIgnoreCase(String prefixNme);

}
