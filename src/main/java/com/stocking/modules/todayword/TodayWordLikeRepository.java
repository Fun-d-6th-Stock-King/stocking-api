package com.stocking.modules.todayword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodayWordLikeRepository extends JpaRepository<TodayWordLike, Long> {

}
