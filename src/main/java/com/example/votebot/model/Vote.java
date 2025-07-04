package com.example.votebot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "votes")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @EmbeddedId
    private VoteId id;

    /* --- связи для составного PK --- */

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private TelegramUser telegramUser;

    @MapsId("candidateId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    /* --- вариант ответа --- */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_code")
    private VoteOption option;

    /* --- метаданные --- */

    @Column(name = "voted_at", nullable = false)
    private OffsetDateTime votedAt = OffsetDateTime.now();
}
