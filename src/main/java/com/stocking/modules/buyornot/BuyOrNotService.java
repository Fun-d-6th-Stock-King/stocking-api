package com.stocking.modules.buyornot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.stocking.modules.buyornot.repo.EvaluateBuySell;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;
import com.stocking.modules.buyornot.repo.EvaluateBuySellRepository;
import com.stocking.modules.buyornot.repo.QEvaluate;
import com.stocking.modules.buyornot.repo.QEvaluateComment;
import com.stocking.modules.buyornot.repo.QEvaluateLike;
import com.stocking.modules.buyornot.vo.BuyOrNotOrder;
import com.stocking.modules.buyornot.vo.BuyOrNotRes;
import com.stocking.modules.buyornot.vo.BuyOrNotRes.SimpleEvaluation;
import com.stocking.modules.buyornot.vo.Comment;
import com.stocking.modules.buyornot.vo.EvaluateBuySellRes;
import com.stocking.modules.buyornot.vo.EvaluationRes;
import com.stocking.modules.buyornot.vo.EvaluationRes.Evaluation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuyOrNotService {

    private final EvaluateBuySellRepository evaluateBuySellRepository;
    
    private final JPAQueryFactory queryFactory;
    
    private static final String LIKECOUNT = "likeCount";
    
    /**
     * 전체 평가 목록 조회
     * @param order
     * @param pageSize
     * @param pageNo
     * @param searchWord
     * @return
     */
    public BuyOrNotRes getBuyOrNotList(BuyOrNotOrder order, long pageSize, long pageNo, String searchWord) {
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        BooleanBuilder builder = new BooleanBuilder();
        Optional.ofNullable(searchWord)
            .ifPresent(vo -> builder.and(qEvaluate.company.contains(vo)));
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = (order == BuyOrNotOrder.LATELY) ? qEvaluate.id.desc() : aliasLikeCount.desc();
        
        List<SimpleEvaluation> simpleEvaluationList = queryFactory.select(
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
        long count = queryFactory.selectFrom(qEvaluate).where(builder).fetchCount();
        
        return BuyOrNotRes.builder()
            .simpleEvaluationList(simpleEvaluationList)
            .pageInfo(
                PageInfo.builder()
                    .pageSize(pageSize)
                    .pageNo(pageNo)
                    .count(count)
                    .build()
            )
            .build();
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
    public EvaluationRes getEvaluationList(long accountId, String stockCode, int order, long pageSize, long pageNo) {
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QAccount qAccount = QAccount.account;
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
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
        long count = queryFactory.selectFrom(qEvaluate).where(qEvaluate.code.eq(stockCode)).fetchCount();
        
        return EvaluationRes.builder()
            .evaluationList(evaluationList)
            .pageInfo(
                PageInfo.builder()
                    .pageSize(pageSize)
                    .pageNo(pageNo)
                    .count(count)
                    .build()
            )
            .build();
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
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
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
        
        if(tuple != null) {
            Long evaluateId = tuple.get(qEvaluateLike.evaluateId);
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
        }else { // 오늘 좋아요를 받은 평가 없는 경우
            return null;
        }
    }
    
    /**
     * 사용자별 살까 말까 선택 정보 저장 
     * @param stockCode
     * @param accountId
     * @param buyOrNot
     * @return
     */
    public Map<String, Object> saveBuySell(String stockCode, long accountId, BuySell buySell) {
        Map<String, Object> resultMap = new HashMap<>();
        
        if(buySell != null) {
            evaluateBuySellRepository.findByCodeAndAccountId(stockCode, accountId).ifPresentOrElse(vo -> {
                vo.setBuySell(buySell);
                evaluateBuySellRepository.save(vo);
                resultMap.put("id", vo.getId());
            }, () -> {
                EvaluateBuySell evaluateBuySell = EvaluateBuySell.builder()
                    .accountId(accountId)
                    .buySell(buySell)
                    .code(stockCode)
                    .accountId(accountId)
                    .build();
                
                evaluateBuySellRepository.save(evaluateBuySell);
                resultMap.put("id", evaluateBuySell.getId());
            });
        }else {
            evaluateBuySellRepository.findByCodeAndAccountId(stockCode, accountId)
                .ifPresent(evaluateBuySellRepository::delete);
        }
        
        return resultMap;
    }
    
    /**
     * 종목별 살래, 말래 개수, 사용자의 해당종목 살래/말래 선택값
     * @param stockCode
     * @param accountId
     * @return
     */
    public EvaluateBuySellRes getBuySellCount(String stockCode, long accountId){
        long buyCount = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.BUY);
        long sellCount = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.SELL);
        
        EvaluateBuySell evaluateBuySell = evaluateBuySellRepository
                .findByCodeAndAccountId(stockCode, accountId).orElse(null);
        
        return EvaluateBuySellRes.builder()
            .code(stockCode)
            .buyCount(buyCount)
            .sellCount(sellCount)
            .evaluateBuySell(evaluateBuySell)
            .build();
    }

}
