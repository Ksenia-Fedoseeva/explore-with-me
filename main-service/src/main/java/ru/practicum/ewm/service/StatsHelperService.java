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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Long, Long> result = new HashMap<>();

        for (Event e : events) {
            Long eventId = e.getId();
            LocalDateTime published = e.getPublishedOn();

            if (published == null) {
                result.put(eventId, 0L);
                continue;
            }

            StatsQueryParams params = new StatsQueryParams(
                    published.format(FORMATTER),
                    LocalDateTime.now().format(FORMATTER),
                    List.of("/events/" + eventId),
                    false
            );

            List<ViewStats> stats = statsClient.getStats(params);

            Long views = stats.isEmpty() ? 0L : stats.get(0).getHits();
            result.put(eventId, views);
        }

        return result;
    }
}
