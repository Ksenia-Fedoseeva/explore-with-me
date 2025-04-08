package ru.practicum.ewm;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String url;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate, @Value("${stats-client.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public void hit(EndpointHit hit) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHit> request = new HttpEntity<>(hit, headers);

        restTemplate.postForEntity(url + "/hit", request, Void.class);
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, boolean unique) {
        StringBuilder uriBuilder = new StringBuilder(url + "/stats?start=" + start + "&end=" + end);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                uriBuilder.append("&uris=").append(uri);
            }
        }

        uriBuilder.append("&unique=").append(unique);

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(uriBuilder.toString(), ViewStats[].class);
        return List.of(response.getBody());
    }
}