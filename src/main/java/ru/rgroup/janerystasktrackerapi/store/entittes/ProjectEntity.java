package ru.rgroup.janerystasktrackerapi.store.entittes;

import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
   private Long id;

    @Column(unique = true)
   private String name;

    @Builder.Default
   private Instant createAt = Instant.now();


   @Builder.Default
   @OneToMany
   private List<TaskStateEntity> taskStates = new ArrayList<>();

}
