package com.stocking.modules.buyornot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
public class BuyOrNotService {

    @Autowired
    private EvaluateRepository buyOrNotRepository;
    
    @Autowired
    private JPAQueryFactory queryFactory;
    
    /**
     * 전체 평가 목록을 페이징 처리하여 조회합니다.
     * @param accountId
     * @param stockCode
     * @param sort
     * @param pageSize
     * @param pageNo
     * @return
     */
    public List<Evaluation> getEvaluationList(int accountId, String stockCode, int sort, int pageSize, int pageNo) {
        
        QEvaluate qEvaluate = QEvaluate.evaluate;
        
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        
        // 평가 ID
        // 종목명
        // 종목코드
        // 장점
        // 단점
        // giphy 이미지 id
        
        // 좋아요 갯수
        // 사용자의 평가에 좋아요 했는지 여부

        // 종목 평가의 댓글(최근 1개)
        // 종목 평가의 댓글단 날짜
        // 종목 평가의 댓글 총 개수
        List<Evaluation> evaluationList = queryFactory.select(
            Projections.fields(Evaluation.class,
                qEvaluate.id,
                qEvaluate.company,
                qEvaluate.code,
                qEvaluate.pros,
                qEvaluate.cons,
                qEvaluate.giphyImgId,
                ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id.count())
                        .from(qEvaluateLike)
                        .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                    "likeCount"),   // 좋아요 횟수
                ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                        .from(qEvaluateLike)
                        .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                                .and(qEvaluateLike.accountId.eq(accountId))).exists(),
                    "userlike")     // 사용자가 좋아요했는지 여부
            )
        ).from(qEvaluate)
        .where(qEvaluate.code.eq(stockCode))
        .fetch();
        
    	return evaluationList;
    }

}
