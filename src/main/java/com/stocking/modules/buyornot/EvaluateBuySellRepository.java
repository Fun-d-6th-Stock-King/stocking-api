package com.stocking.modules.buyornot;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateBuySellRepository extends JpaRepository<EvaluateBuySell, Integer> {
    
    public Optional<EvaluateBuySell> findByCodeAndAccountId(String stockCode, int accountId);
    
    public long countByCodeAndBuySell(String stockCode, BuySell buySell);

}
