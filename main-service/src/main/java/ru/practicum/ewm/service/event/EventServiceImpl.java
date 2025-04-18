package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.RequestStatus;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.StatsHelperService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsHelperService statsHelperService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> findEvents(EventSearchParams p) {

        LocalDateTime start = Optional.ofNullable(p.getRangeStart()).orElse(LocalDateTime.now());
        LocalDateTime end = Optional.ofNullable(p.getRangeEnd()).orElse(LocalDateTime.now().plusYears(100));

        Pageable pageable = PageRequest.of(p.getFrom() / p.getSize(), p.getSize());

        Page<Event> page = eventRepository.findEventsForAdmin(
                p.getUsers(),
                p.getStates(),
                p.getCategories(),
                start,
                end,
                pageable
        );

        List<Event> events = page.getContent();

        Map<Long, Long> confirmedMap = requestRepository.findByEventIdInAndStatus(
                        events.stream()
                                .map(Event::getId)
                                .toList(),
                        RequestStatus.CONFIRMED).stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

        Map<Long, Long> viewsMap = statsHelperService.getViewsByEvents(events);

        return events.stream()
                .map(e -> EventMapper.toFullDto(
                        e,
                        confirmedMap.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено: " + eventId));

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Событие не в состоянии ожидания");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Опубликованное событие нельзя отклонить");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        Long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        Long views = statsHelperService.getViews(event.getId(), event.getPublishedOn());

        return EventMapper.toFullDto(eventRepository.save(event), confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена: " + newEventDto.getCategory()));

        Event event = EventMapper.toEntity(newEventDto, initiator, category);

        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        return EventMapper.toFullDto(eventRepository.save(event), 0L, 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или не принадлежит пользователю"));

        Long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        Long views = statsHelperService.getViews(event.getId(), event.getPublishedOn());

        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или пользователь не является инициатором"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Редактировать можно только ожидающие или отменённые события");
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {

        LocalDateTime start = Optional.ofNullable(params.getRangeStart()).orElse(LocalDateTime.now());
        LocalDateTime end   = Optional.ofNullable(params.getRangeEnd()).orElse(LocalDateTime.now().plusYears(100));

        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        Pageable page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        Page<Event> eventsPage = eventRepository.findAllPublicEvents(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                start,
                end,
                page
        );

        statsClient.hit(request);

        return eventsPage.getContent().stream()
                .map(e -> {
                    Long confirmed = requestRepository
                            .countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED);
                    Long views = statsHelperService.getViews("/events/" + e.getId(), e.getPublishedOn());
                    return EventMapper.toShortDto(e, confirmed, views);
                })
                .toList();
    }


    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено или не опубликовано"));

        statsClient.hit(request);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        Long views = statsClient.getUniqueViews("/events/" + eventId);

        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

}
