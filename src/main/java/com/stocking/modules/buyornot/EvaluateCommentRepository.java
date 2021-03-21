package com.stocking.modules.buyornot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateCommentRepository extends JpaRepository<EvaluateComment, Integer> {

}
