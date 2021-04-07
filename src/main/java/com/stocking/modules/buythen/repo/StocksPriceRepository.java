package com.stocking.modules.buythen.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StocksPriceRepository extends JpaRepository<StocksPrice, Long> {

    public Optional<StocksPrice> findByStocksId(long stocksId);
}
