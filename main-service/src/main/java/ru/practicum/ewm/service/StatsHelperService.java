package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.StatsQueryParams;
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

    public long getViews(Long eventId, LocalDateTime publishedOn) {
        if (publishedOn == null) {
            return 0L;
        }

        StatsQueryParams params = new StatsQueryParams(
                publishedOn.format(FORMATTER),
                LocalDateTime.now().format(FORMATTER),
                List.of("/events/" + eventId),
                false
        );

        List<ViewStats> stats = statsClient.getStats(params);

        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }

    public long getViews(String uri, LocalDateTime publishedOn) {
        if (publishedOn == null) {
            return 0L;
        }

        StatsQueryParams params = new StatsQueryParams(
                publishedOn.format(FORMATTER),
                LocalDateTime.now().format(FORMATTER),
                List.of(uri),
                false
        );

        List<ViewStats> stats = statsClient.getStats(params);

        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }

    public Map<Long, Long> getViewsByEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, LocalDateTime> idToPublished = events.stream()
                .filter(e -> e.getPublishedOn() != null)
                .collect(Collectors.toMap(Event::getId, Event::getPublishedOn));

        if (idToPublished.isEmpty()) {
            return events.stream().collect(Collectors.toMap(Event::getId, e -> 0L));
        }

        List<String> uris = idToPublished.keySet().stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime start = Collections.min(idToPublished.values());

        StatsQueryParams params = new StatsQueryParams(
                start.format(FORMATTER),
                LocalDateTime.now().format(FORMATTER),
                uris,
                false
        );

        Map<Long, Long> result = statsClient.getStats(params).stream()
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(stat.getUri().replace("/events/", "")),
                        ViewStats::getHits
                ));

        idToPublished.keySet().forEach(id -> result.putIfAbsent(id, 0L));

        return result;
    }
}