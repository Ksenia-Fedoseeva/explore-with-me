package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {
    @NotBlank
    @Size(max = 50)
    private String title;

    private Boolean pinned;

    private List<Long> events;
}
