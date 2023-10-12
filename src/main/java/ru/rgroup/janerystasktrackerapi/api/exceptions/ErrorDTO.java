package ru.rgroup.janerystasktrackerapi.api.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}
