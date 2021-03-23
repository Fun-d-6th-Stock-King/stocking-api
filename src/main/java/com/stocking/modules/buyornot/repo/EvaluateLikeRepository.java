package com.stocking.modules.buyornot.repo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateLikeRepository extends JpaRepository<EvaluateLike, Long> {

    Optional<EvaluateLike> findByEvaluateIdAndAccountId(long evaluateId, long accountId);
    
    Optional<EvaluateLike> findByCreatedDateBetween(LocalDateTime startDt, LocalDateTime endDt);
}

