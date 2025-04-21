package ru.practicum.ewm.service.comment;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.model.enums.CommentStatus;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateUserComment(Long userId, Long eventId, Long commentId, NewCommentDto dto);

    void deleteUserComment(Long userId, Long eventId, Long commentId);

    List<CommentDto> getCommentsByEvent(Long eventId);

    List<CommentDto> getUserCommentsOnEvent(Long userId, Long eventId);

    List<CommentDto> getUserComments(Long userId);

    CommentDto getUserComment(Long userId, Long eventId, Long commentId);

    CommentDto moderateComment(Long commentId, CommentStatus newStatus);

    void deleteCommentByAdmin(Long commentId);
}
