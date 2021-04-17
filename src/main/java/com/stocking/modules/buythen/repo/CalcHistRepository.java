package com.stocking.modules.buythen.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalcHistRepository extends JpaRepository<CalcHist, Long> {

}
