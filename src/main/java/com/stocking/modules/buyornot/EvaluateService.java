package com.stocking.modules.buyornot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.buyornot.repo.Evaluate;
import com.stocking.modules.buyornot.repo.EvaluateComment;
import com.stocking.modules.buyornot.repo.EvaluateCommentRepository;
import com.stocking.modules.buyornot.repo.EvaluateLike;
import com.stocking.modules.buyornot.repo.EvaluateLikeRepository;
import com.stocking.modules.buyornot.repo.EvaluateRepository;
import com.stocking.modules.buyornot.repo.QEvaluate;
import com.stocking.modules.buyornot.repo.QEvaluateComment;
import com.stocking.modules.buyornot.repo.QEvaluateLike;
import com.stocking.modules.buyornot.vo.Comment;
import com.stocking.modules.buyornot.vo.EvaluateReq;
import com.stocking.modules.buyornot.vo.EvaluationDetailRes;
import com.stocking.modules.buyornot.vo.EvaluationDetailRes.Evaluation;
import com.stocking.modules.stock.Stock;
import com.stocking.modules.stock.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluateService {
    
    private final EvaluateRepository evaluateRepository;
    
    private final EvaluateCommentRepository evaluateCommentRepository;
    
    private final EvaluateLikeRepository evaluateLikeRepository;
    
    private final StockRepository stockRepository;
    
    private final JPAQueryFactory queryFactory;
    
    /**
     * 좋아요 저장 / 삭제
     * @param evaluateId
     * @param uid
     * @return
     */
    public Map<String, Object> saveLike(long evaluateId, String uid) {
        EvaluateLike evaluateLike = EvaluateLike.builder()
            .uid(uid)
            .evaluateId(evaluateId)
            .build();
        
        Map<String, Object> resultMap = new HashMap<>();
        
        evaluateLikeRepository.findByEvaluateIdAndUid(evaluateId, uid)
            .ifPresentOrElse(
                vo -> {
                    resultMap.put("id", vo.getId());
                    resultMap.put("like", false);
                    evaluateLikeRepository.delete(vo);
                },
                () -> {
                    evaluateLikeRepository.save(evaluateLike);
                    resultMap.put("id", evaluateLike.getId());
                    resultMap.put("like", true);
                }
            );
        return resultMap;
    }
    
    /**
     * 평가 저장
     * @param evaluateReq
     * @param uid
     * @return
     * @throws Exception
     */
    public Evaluate saveEvaluate(EvaluateReq evaluateReq, String uid) throws Exception {
        Stock stock = stockRepository.findByCode(evaluateReq.getCode())
                .orElseThrow(() -> new Exception("종목코드가 없습니다."));
        
        Evaluate evaluate = Evaluate.builder()
            .company(stock.getCompany())
            .code(stock.getCode())
            .pros(evaluateReq.getPros())
            .cons(evaluateReq.getCons())
            .giphyImgId(evaluateReq.getGiphyImgId())
            .createdUid(uid)
            .build();
        
        return evaluateRepository.save(evaluate);
    }
    
    /**
     * 코멘트 저장
     * @param evaluateId
     * @param comment
     * @param uid
     * @return
     */
    public EvaluateComment saveComment(long evaluateId, String comment, String uid) {
        EvaluateComment evaluateComment = EvaluateComment.builder()
            .evaluateId(evaluateId)
            .comment(comment)
            .createdUid(uid)
            .build();
        return evaluateCommentRepository.save(evaluateComment);
    }
    
    /**
     * 평가 상세
     * @param evaluateId
     * @param uid
     * @return
     */
    public EvaluationDetailRes getDetail(long evaluateId, FirebaseUser user) {
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        }
        
        Evaluation evaluation = queryFactory.select(
                Projections.fields(Evaluation.class,
                    qEvaluate.id,
                    qEvaluate.company,
                    qEvaluate.code,
                    qEvaluate.pros,
                    qEvaluate.cons,
                    qEvaluate.giphyImgId,
                    qEvaluate.createdUid.as("uid"),
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id.count())
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                        aliasLikeCount),   // 좋아요 횟수
                    userLike     // 사용자가 좋아요했는지 여부
                )
            ).from(qEvaluate)
            .where(qEvaluate.id.eq(evaluateId))
            .fetchOne();
        
        List<Comment> commentList = queryFactory.select(
                Projections.fields(Comment.class,
                    qEvaluateComment.id,
                    qEvaluateComment.comment,
                    qEvaluateComment.createdUid.as("uid"),
                    qEvaluateComment.createdDate
                )
            ).from(qEvaluateComment)
            .where(qEvaluateComment.evaluateId.eq(evaluateId))
            .fetch();
        
        return EvaluationDetailRes.builder()
            .evaluation(evaluation)
            .commentList(commentList)
            .build();
    }
}
