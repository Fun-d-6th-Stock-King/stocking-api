package com.stocking.modules.buythen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OldStockRepository extends JpaRepository<OldStock, Long> {

    Optional<OldStock> findByCompany(String company);
    Optional<OldStock> findByCode(String company);
}
