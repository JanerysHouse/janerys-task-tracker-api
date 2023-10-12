package ru.rgroup.janerystasktrackerapi.api.dto;

import lombok.*;

import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Instant createAt;
}
