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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class TelegramUser {

    @Id
    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "username")
    private String username;

    @OneToMany(mappedBy = "telegramUser", fetch = FetchType.LAZY)
    private Set<Vote> votes;
}
