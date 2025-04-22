package ru.practicum.ewm.dto.comment;

import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.model.enums.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime createdOn;

    private CommentStatus status;
}
