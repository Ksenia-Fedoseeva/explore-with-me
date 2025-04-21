package ru.practicum.ewm.controller.prvt;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody NewCommentDto dto) {
        return commentService.addComment(userId, eventId, dto);
    }

    @PatchMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto updateUserComment(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @PathVariable Long commentId,
                                        @Valid @RequestBody NewCommentDto dto) {
        return commentService.updateUserComment(userId, eventId, commentId, dto);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getUserCommentsOnEvent(@PathVariable Long userId,
                                                   @PathVariable Long eventId) {
        return commentService.getUserCommentsOnEvent(userId, eventId);
    }

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto getUserComment(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @PathVariable Long commentId) {
        return commentService.getUserComment(userId, eventId, commentId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }

    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @PathVariable Long commentId) {
        commentService.deleteUserComment(userId, eventId, commentId);
    }
}
