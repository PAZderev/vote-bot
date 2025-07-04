package com.example.votebot.repository;

import com.example.votebot.model.Candidate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    @Query("""
           SELECT c
           FROM Candidate c
           WHERE NOT EXISTS (
               SELECT v FROM Vote v
               WHERE v.candidate = c
                 AND v.telegramUser.telegramId = :userId
           )
           ORDER BY c.id
           """)
    List<Candidate> findUnvoted(@Param("userId") Long userId, Pageable pageable);

    Optional<Candidate> findTopByVotesTelegramUserTelegramIdNotOrderById(Long userId);

}
