package com.stocking.modules.buyornot.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;

@Repository
public interface EvaluateBuySellRepository extends JpaRepository<EvaluateBuySell, Long> {
    
    public Optional<EvaluateBuySell> findByCodeAndUid(String stockCode, String uid);
    
    public long countByCodeAndBuySell(String stockCode, BuySell buySell);

}
