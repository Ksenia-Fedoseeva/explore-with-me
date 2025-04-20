package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;

import java.util.List;

public interface EventService {
    List<EventFullDto> findEvents(EventSearchParams p);

    @Transactional
    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    @Transactional(readOnly = true)
    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    @Transactional(readOnly = true)
    EventFullDto getUserEvent(Long userId, Long eventId);

    @Transactional
    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);
}
