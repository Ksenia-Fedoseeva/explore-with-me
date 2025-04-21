package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.CommentStatus;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto addComment(Long userId, NewCommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NotFoundException("Событие с id " + dto.getEventId() + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Оставлять комментарии можно только к опубликованным событиям");
        }

        Comment comment = CommentMapper.toEntity(dto, user, event);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateUserComment(Long userId, UpdateCommentDto dto) {
        Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + dto.getCommentId() + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Вы можете редактировать только свои комментарии");
        }

        if (!comment.getEvent().getId().equals(dto.getEventId())) {
            throw new ConflictException("Комментарий не принадлежит данному событию");
        }

        comment.setText(dto.getText());
        comment.setStatus(CommentStatus.PENDING);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteUserComment(Long userId, Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Вы можете удалить только свои комментарии");
        }

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Комментарий не принадлежит данному событию");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED, pageable)
                .stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getApprovedCommentById(Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Комментарий не относится к событию");
        }

        if (!comment.getStatus().equals(CommentStatus.APPROVED)) {
            throw new ConflictException("Комментарий ещё не прошёл модерацию");
        }

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserCommentsOnEvent(Long userId, Long eventId) {
        return commentRepository.findByAuthorIdAndEventId(userId, eventId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId) {
        return commentRepository.findByAuthorId(userId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getUserComment(Long userId, Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Вы можете просматривать только свои комментарии");
        }

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Комментарий не относится к указанному событию");
        }

        return CommentMapper.toDto(comment);
    }

    @Override
    public CommentDto moderateComment(Long commentId, CommentStatus newStatus) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        comment.setStatus(newStatus);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с id " + commentId + " не найден");
        }
        commentRepository.deleteById(commentId);
    }
}
