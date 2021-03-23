package com.stocking.modules.buyornot.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;

@Repository
public interface EvaluateBuySellRepository extends JpaRepository<EvaluateBuySell, Long> {
    
    public Optional<EvaluateBuySell> findByCodeAndAccountId(String stockCode, long accountId);
    
    public long countByCodeAndBuySell(String stockCode, BuySell buySell);

}
