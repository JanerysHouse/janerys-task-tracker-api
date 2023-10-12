package ru.rgroup.janerystasktrackerapi.api.dto;

import jakarta.persistence.Column;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.NotFound;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStateDto {

    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Long ordinal;
    @NonNull
    private Instant createAt;
}
