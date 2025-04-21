package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getApprovedComments(@PathVariable Long eventId,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        return commentService.getCommentsByEvent(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getApprovedComment(@PathVariable Long eventId,
                                         @PathVariable Long commentId) {
        return commentService.getApprovedCommentById(eventId, commentId);
    }
}
