package com.example.votebot.repository;

import com.example.votebot.model.Vote;
import com.example.votebot.model.VoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, VoteId> { }
