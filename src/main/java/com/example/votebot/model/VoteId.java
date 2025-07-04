package com.example.votebot.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class VoteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "candidate_id")
    private Integer candidateId;
}
