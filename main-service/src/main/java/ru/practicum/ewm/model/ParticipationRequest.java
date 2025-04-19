package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.ewm.model.enums.RequestStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "participation_requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private RequestStatus status;
}