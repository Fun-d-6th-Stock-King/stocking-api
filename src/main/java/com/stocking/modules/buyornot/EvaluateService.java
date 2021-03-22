package com.stocking.modules.buyornot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.modules.account.QAccount;
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
     * @param accountId
     * @return
     */
    public Map<String, Object> saveLike(int evaluateId, long accountId) {
        EvaluateLike evaluateLike = new EvaluateLike();
        evaluateLike.setAccountId(accountId);
        evaluateLike.setEvaluateId(evaluateId);
        
        Map<String, Object> resultMap = new HashMap<>();
        
        evaluateLikeRepository.findByEvaluateIdAndAccountId(evaluateId, accountId)
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
     * @param accountId
     * @return
     * @throws Exception
     */
    public Evaluate saveEvaluate(EvaluateReq evaluateReq, long accountId) throws Exception {
        Evaluate evaluate = new Evaluate();
        
        Stock stock = stockRepository.findByCode(evaluateReq.getCode())
                .orElseThrow(() -> new Exception("종목코드가 없습니다."));
        
        evaluate.setCompany(stock.getCompany());
        evaluate.setCode(stock.getCode());
        evaluate.setPros(evaluateReq.getPros());
        evaluate.setCons(evaluateReq.getCons());
        evaluate.setGiphyImgId(evaluateReq.getGiphyImgId());
        evaluate.setCreatedId(accountId);
        
        return evaluateRepository.save(evaluate);
    }
    
    /**
     * 코멘트 저장
     * @param evaluateId
     * @param comment
     * @param accountId
     * @return
     */
    public EvaluateComment saveComment(int evaluateId, String comment, long accountId) {
        
        EvaluateComment evaluateComment = new EvaluateComment();
        evaluateComment.setEvaluateId(evaluateId);
        evaluateComment.setComment(comment);
        evaluateComment.setCreatedId(accountId);
        evaluateCommentRepository.save(evaluateComment);
        
        return evaluateComment;
    }
    
    /**
     * 평가 상세
     * @param evaluateId
     * @param accountId
     * @return
     */
    public EvaluationDetailRes getDetail(long evaluateId, long accountId) {
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        Evaluation evaluation = queryFactory.select(
                Projections.fields(Evaluation.class,
                    qEvaluate.id,
                    qEvaluate.company,
                    qEvaluate.code,
                    qEvaluate.pros,
                    qEvaluate.cons,
                    qEvaluate.giphyImgId,
                    qAccount.uuid,
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id.count())
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                        aliasLikeCount),   // 좋아요 횟수
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id)
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                                    .and(qEvaluateLike.accountId.eq(accountId))).exists(),
                        "userlike")     // 사용자가 좋아요했는지 여부
                )
            ).from(qEvaluate)
            .innerJoin(qAccount).on(qEvaluate.createdId.eq(qAccount.id))
            .where(qEvaluate.id.eq(evaluateId))
            .fetchOne();
        
        List<Comment> commentList = queryFactory.select(
                Projections.fields(Comment.class,
                    qEvaluateComment.id,
                    qEvaluateComment.comment,
                    qEvaluateComment.createdDate,
                    qAccount.uuid
                )
            ).from(qEvaluateComment)
            .innerJoin(qAccount).on(qEvaluateComment.createdId.eq(qAccount.id))
            .where(qEvaluateComment.evaluateId.eq(evaluateId))
            .fetch();
        
        return new EvaluationDetailRes(evaluation, commentList);
    }
}
