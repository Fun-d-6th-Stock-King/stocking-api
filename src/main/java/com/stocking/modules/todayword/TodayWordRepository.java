package com.stocking.modules.todayword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodayWordRepository extends JpaRepository<TodayWord, Long> {

    public Optional<TodayWord> findByIdAndCreatedUid(long Id, String createdUid);
}
