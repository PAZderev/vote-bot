package com.example.votebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vote_options")
@Getter
@Setter
@NoArgsConstructor
public class VoteOption {

    @Id
    @Column(length = 1)
    private String code;          // 'Y', 'N', 'A'

    @Column(nullable = false, unique = true)
    private String label;         // «Да», «Нет», «Воздержусь»

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
    private Set<Vote> votes;
}
