package ru.practicum.ewm.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentDto {

    @NotNull
    private Long commentId;

    @NotNull
    private Long eventId;

    @NotBlank
    @Size(min = 3, max = 5000)
    private String text;
}
