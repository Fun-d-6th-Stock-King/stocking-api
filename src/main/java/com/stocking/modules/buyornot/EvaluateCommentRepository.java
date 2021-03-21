package com.stocking.modules.buyornot;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateCommentRepository extends JpaRepository<EvaluateComment, Integer> {
    
    Optional<List<EvaluateComment>> findAllByEvaluateId(int evaluateId, Sort sort);
}
