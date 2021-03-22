package com.stocking.modules.buyornot;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateLikeRepository extends JpaRepository<EvaluateLike, Integer> {

    Optional<EvaluateLike> findByEvaluateIdAndAccountId(int evaluateId, int accountId);
    
    Optional<EvaluateLike> findByCreatedDateBetween(LocalDateTime startDt, LocalDateTime endDt);
}

