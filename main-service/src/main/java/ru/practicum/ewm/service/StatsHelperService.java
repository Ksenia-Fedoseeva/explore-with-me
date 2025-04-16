package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatsHelperService {

    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long getViews(Long eventId) {
        return getViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        List<ViewStats> stats = statsClient.getStats(
                LocalDateTime.now().minusYears(10).format(FORMATTER),
                LocalDateTime.now().plusYears(10).format(FORMATTER),
                uris,
                false
        );

        return stats.stream().collect(Collectors.toMap(
                s -> Long.parseLong(s.getUri().replace("/events/", "")),
                ViewStats::getHits
        ));
    }

    public long getViews(String uri) {
        List<ViewStats> stats = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0).format(FORMATTER),
                LocalDateTime.now().format(FORMATTER),
                List.of(uri),
                false
        );

        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }

    public Map<Long, Long> getViewsByEvents(List<Event> events) {
        List<Long> ids = events.stream()
                .map(Event::getId)
                .toList();
        return getViews(ids);
    }
}
