package com.stocking.modules.buyornot.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluateCommentRepository extends JpaRepository<EvaluateComment, Long> {
    
    Optional<List<EvaluateComment>> findAllByEvaluateId(long evaluateId, Sort sort);
}
