package com.stocking.modules.buythen.repo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    public Optional<StockHistory> findByCodeAndDate(String code, LocalDateTime date);

}
