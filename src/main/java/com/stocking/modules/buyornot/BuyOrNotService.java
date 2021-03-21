package com.stocking.modules.buyornot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.modules.account.QAccount;
import com.stocking.modules.buyornot.EvaluationRes.Evaluation;
import com.stocking.modules.buyornot.EvaluationRes.PageInfo;

@Service
public class BuyOrNotService {

//    @Autowired
//    private EvaluateRepository buyOrNotRepository;
    
    @Autowired
    private EvaluateLikeRepository evaluateLikeRepository;
    
    @Autowired
    private JPAQueryFactory queryFactory;
    
    /**
     * 전체 평가 목록을 페이징 처리하여 조회합니다.
     * @param accountId
     * @param stockCode
     * @param order
     * @param pageSize
     * @param pageNo
     * @return
     */
    public EvaluationRes getEvaluationList(int accountId, String stockCode, int order, int pageSize, int pageNo) {
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = (order == 1) ? qEvaluate.id.desc() : aliasLikeCount.desc();
        
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
        .where(qEvaluate.code.eq(stockCode))
        .orderBy(orderSpecifier)
        .offset((pageNo - 1) * pageSize)
        .limit(pageSize)
        .fetch();
        
        // 전체 건수
        long cnt = queryFactory.selectFrom(qEvaluate).where(qEvaluate.code.eq(stockCode)).fetchCount();
        
        return new EvaluationRes(evaluationList, new PageInfo(pageSize, pageNo, cnt));
    }
    
    /**
     * 오늘 베스트를 가장 많이 받은 평가
     * @return
     */
    public Evaluation getTodayBest() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
//        evaluateLikeRepository.findByCreatedDateBetween(now.atTime(0, 0, 0), now.atTime(23, 59, 59));
        // 오늘 날짜로 조회해서 평가 id 별로 그룹핑해서 좋아요가 가장 많은 평가 아이디를 가져와야함. 
        // querydsl 사용해야할듯..  조건이랑 그룹핑 때문에.. 
        
        return null;
    }

}
