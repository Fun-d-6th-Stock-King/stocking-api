package com.stocking.modules.todayword.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodayWordLikeRepository extends JpaRepository<TodayWordLike, Long> {
    
    public Optional<TodayWordLike> findByTodayWordIdAndCreatedUid(long todayWordId, String createdUid);
}
