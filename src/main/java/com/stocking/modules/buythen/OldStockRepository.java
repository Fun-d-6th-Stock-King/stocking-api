package com.stocking.modules.buythen;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OldStockRepository extends JpaRepository<OldStock, Long> {

    Optional<OldStock> findByCompany(String company);
}
