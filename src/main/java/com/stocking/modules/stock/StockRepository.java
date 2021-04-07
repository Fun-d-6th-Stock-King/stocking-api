package com.stocking.modules.stock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    public Optional<Stock> findByCode(String code);
    
    public Optional<List<Stock>> findAllByMarket(String market);
}
