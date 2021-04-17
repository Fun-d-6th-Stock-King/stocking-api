package com.stocking.modules.todayword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodayWordRepository extends JpaRepository<TodayWord, Long> {

}
