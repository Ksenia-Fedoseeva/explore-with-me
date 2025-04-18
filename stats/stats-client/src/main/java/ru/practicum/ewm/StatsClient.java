package ru.practicum.ewm;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.StatsQueryParams;
import ru.practicum.ewm.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stats-client.app-name}")
    private String appName;

    public StatsClient(RestTemplate restTemplate, @Value("${stats-client.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public void hit(HttpServletRequest request) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(appName);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHit> entity = new HttpEntity<>(hit, headers);

        restTemplate.postForEntity(url + "/hit", entity, Void.class);
    }

    public List<ViewStats> getStats(StatsQueryParams params) {
        StringBuilder uriBuilder = new StringBuilder(url + "/stats?start=" + params.getStart() + "&end=" + params.getEnd());

        if (params.getUris() != null && !params.getUris().isEmpty()) {
            for (String uri : params.getUris()) {
                uriBuilder.append("&uris=").append(uri);
            }
        }

        uriBuilder.append("&unique=").append(params.isUnique());

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(uriBuilder.toString(), ViewStats[].class);
        return List.of(response.getBody());
    }

    public long getUniqueViews(String uri) {
        String start = "2000-01-01 00:00:00";
        String end = LocalDateTime.now().format(formatter);
        StatsQueryParams params = new StatsQueryParams(start, end, List.of(uri), true);
        List<ViewStats> stats = getStats(params);

        return stats.isEmpty() ? 0 : stats.get(0).getHits();
    }
}