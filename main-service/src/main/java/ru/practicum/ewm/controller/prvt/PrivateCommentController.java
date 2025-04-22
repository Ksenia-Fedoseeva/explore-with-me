package ru.practicum.ewm.controller.prvt;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @Valid @RequestBody NewCommentDto dto) {
        return commentService.addComment(userId, dto);
    }

    @PatchMapping
    public CommentDto updateUserComment(@PathVariable Long userId,
                                        @Valid @RequestBody UpdateCommentDto dto) {
        return commentService.updateUserComment(userId, dto);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }

    @GetMapping("/event")
    public List<CommentDto> getUserCommentsOnEvent(@PathVariable Long userId,
                                                   @RequestParam Long eventId) {
        return commentService.getUserCommentsOnEvent(userId, eventId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getUserComment(@PathVariable Long userId,
                                     @PathVariable Long commentId,
                                     @RequestParam Long eventId) {
        return commentService.getUserComment(userId, eventId, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserComment(@PathVariable Long userId,
                                  @PathVariable Long commentId,
                                  @RequestParam Long eventId) {
        commentService.deleteUserComment(userId, eventId, commentId);
    }
}
