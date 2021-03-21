package com.stocking.modules.buyornot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.PageInfo;
import com.stocking.modules.account.QAccount;
import com.stocking.modules.buyornot.BuyOrNotRes.SimpleEvaluation;
import com.stocking.modules.buyornot.EvaluationRes.Evaluation;

@Service
public class BuyOrNotService {

//    @Autowired
//    private EvaluateRepository buyOrNotRepository;
    
    @Autowired
    private EvaluateLikeRepository evaluateLikeRepository;
    
    @Autowired
    private EvaluateBuySellRepository evaluateBuySellRepository;
    
    @Autowired
    private JPAQueryFactory queryFactory;
    
    /**
     * 전체 평가 목록 조회
     * @param order
     * @param pageSize
     * @param pageNo
     * @param searchWord
     * @return
     */
    public BuyOrNotRes getBuyOrNotList(BuyOrNotOrder order, int pageSize, int pageNo, String searchWord) {
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if(searchWord != null) {
            builder.and(qEvaluate.company.contains(searchWord));
        } 
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = (order == BuyOrNotOrder.LATELY) ? qEvaluate.id.desc() : aliasLikeCount.desc();
        
        List<SimpleEvaluation> list = queryFactory.select(
                Projections.fields(SimpleEvaluation.class,
                    qEvaluate.id,
                    qEvaluate.code,
                    qEvaluate.company,
                    qEvaluate.pros,
                    qEvaluate.cons,
                    qAccount.uuid,
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id.count())
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                        aliasLikeCount)   // 좋아요 횟수
                )
            ).from(qEvaluate)
            .innerJoin(qAccount).on(qEvaluate.createdId.eq(qAccount.id))
            .where(builder)
            .orderBy(orderSpecifier)
            .offset((pageNo - 1) * pageSize)
            .limit(pageSize)
            .fetch();
        
        // 전체 건수
        long cnt = queryFactory.selectFrom(qEvaluate).where(builder).fetchCount();
        
        return new BuyOrNotRes(list, new PageInfo(pageSize, pageNo, cnt));
    }
    
    /**
     * 종목별 평가 목록을 페이징 처리하여 조회합니다.
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
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = (order == 1) ? qEvaluate.id.desc() : aliasLikeCount.desc();
        
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
        
        evaluationList = evaluationList.stream().map(vo -> {
            long commentCount = queryFactory
                .selectFrom(qEvaluateComment)
                .where(qEvaluateComment.evaluateId.eq(vo.getId()))
                .fetchCount();
            vo.setCommentCount(commentCount);
            
            if(commentCount > 0) {
                Comment comment = queryFactory
                    .select(
                        Projections.fields(Comment.class,
                            qEvaluateComment.id,
                            qEvaluateComment.comment,
                            qEvaluateComment.createdDate,
                            qAccount.uuid
                            )
                        )
                    .from(qEvaluateComment)
                    .innerJoin(qAccount).on(qEvaluateComment.createdId.eq(qAccount.id))
                    .where(qEvaluateComment.evaluateId.eq(vo.getId()))
                    .orderBy(qEvaluateComment.createdDate.desc())
                    .limit(1)
                    .fetchOne();
                vo.setRecentComment(comment);
            }
            return vo;
        }).collect(Collectors.toList());
        
        // 전체 건수
        long cnt = queryFactory.selectFrom(qEvaluate).where(qEvaluate.code.eq(stockCode)).fetchCount();
        
        return new EvaluationRes(evaluationList, new PageInfo(pageSize, pageNo, cnt));
    }
    
    /**
     * 오늘 베스트를 가장 많이 받은 평가
     * @return
     */
    public SimpleEvaluation getTodayBest() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        NumberPath<Long> groupCount = Expressions.numberPath(Long.class, "groupCount");
        
        Tuple tuple = queryFactory.select(
                qEvaluateLike.evaluateId,
                qEvaluateLike.id.count().as(groupCount)
            )
            .from(qEvaluateLike)
            .where(qEvaluateLike.createdDate.between(now.atTime(0, 0, 0), now.atTime(23, 59, 59)))
            .groupBy(qEvaluateLike.evaluateId)
            .orderBy(groupCount.desc())
            .limit(1)
            .fetchOne();
        
        int evaluateId = tuple.get(qEvaluateLike.evaluateId);
        
        return queryFactory.select(
              Projections.fields(SimpleEvaluation.class,
                  qEvaluate.id,
                  qEvaluate.code,
                  qEvaluate.company,
                  qEvaluate.pros,
                  qEvaluate.cons,
                  qAccount.uuid,
                  ExpressionUtils.as(
                      JPAExpressions.select(qEvaluateLike.id.count())
                          .from(qEvaluateLike)
                          .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                      aliasLikeCount)   // 좋아요 횟수
              )
          ).from(qEvaluate)
          .innerJoin(qAccount).on(qEvaluate.createdId.eq(qAccount.id))
          .where(qEvaluate.id.eq(evaluateId))
          .fetchOne();
    }
    
    /**
     * 사용자별 살까 말까 선택 정보 저장 
     * @param stockCode
     * @param accountId
     * @param buyOrNot
     * @return
     */
    public Map<String, Object> saveBuySell(String stockCode, int accountId, BuySell buySell) {
        Map<String, Object> resultMap = new HashMap<>();
        
        if(buySell != null) {
            evaluateBuySellRepository.findByCodeAndAccountId(stockCode, accountId).ifPresentOrElse(vo -> {
                vo.setBuySell(buySell);
                evaluateBuySellRepository.save(vo);
                resultMap.put("id", vo.getId());
            }, () -> {
                EvaluateBuySell evaluateBuySell = new EvaluateBuySell();
                evaluateBuySell.setAccountId(accountId);
                evaluateBuySell.setBuySell(buySell);
                evaluateBuySell.setCode(stockCode);
                evaluateBuySellRepository.save(evaluateBuySell);
                resultMap.put("id", evaluateBuySell.getId());
            });
        }else {
            evaluateBuySellRepository.findByCodeAndAccountId(stockCode, accountId)
                .ifPresent(vo -> {
                    evaluateBuySellRepository.delete(vo);
                });
        }
        
        return resultMap;
    }
    
    /**
     * 종목별 살래, 말래 개수, 사용자의 해당종목 살래/말래 선택값
     * @param stockCode
     * @param accountId
     * @return
     */
    public EvaluateBuySellRes getBuySellCount(String stockCode, int accountId){
        long buyCnt = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.BUY);
        long sellCnt = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.SELL);
        
        EvaluateBuySell evaluateBuySell = evaluateBuySellRepository.findByCodeAndAccountId(stockCode, accountId)
                .orElse(new EvaluateBuySell());
        
        return new EvaluateBuySellRes(stockCode, buyCnt, sellCnt, evaluateBuySell.getBuySell());
    }

}
