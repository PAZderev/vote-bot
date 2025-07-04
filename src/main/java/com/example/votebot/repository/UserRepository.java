package com.example.votebot.repository;

import com.example.votebot.model.Candidate;
import com.example.votebot.model.TelegramUser;
import com.example.votebot.model.Vote;
import com.example.votebot.model.VoteId;
import com.example.votebot.model.VoteOption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<TelegramUser, Long> {

}

