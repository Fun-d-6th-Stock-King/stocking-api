package com.stocking.modules.buyornot;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.common.PageInfo;
import com.stocking.infra.common.StockUtils;
import com.stocking.infra.common.StockUtils.RealTimeStock;
import com.stocking.modules.buyornot.repo.EvaluateBuySell;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;
import com.stocking.modules.buyornot.repo.EvaluateBuySellRepository;
import com.stocking.modules.buyornot.repo.QEvaluate;
import com.stocking.modules.buyornot.repo.QEvaluateBuySell;
import com.stocking.modules.buyornot.repo.QEvaluateComment;
import com.stocking.modules.buyornot.repo.QEvaluateLike;
import com.stocking.modules.buyornot.vo.BuyOrNotOrder;
import com.stocking.modules.buyornot.vo.BuyOrNotPeriod;
import com.stocking.modules.buyornot.vo.BuyOrNotRes;
import com.stocking.modules.buyornot.vo.BuyOrNotRes.SimpleEvaluation;
import com.stocking.modules.buyornot.vo.BuySellRankRes;
import com.stocking.modules.buyornot.vo.BuySellRankRes.GroupResult;
import com.stocking.modules.buyornot.vo.BuySellRankRes.RankListType;
import com.stocking.modules.buyornot.vo.BuySellRankRes.RankTop;
import com.stocking.modules.buyornot.vo.Comment;
import com.stocking.modules.buyornot.vo.EvaluateBuySellRes;
import com.stocking.modules.buyornot.vo.EvaluationRes;
import com.stocking.modules.buyornot.vo.EvaluationRes.Evaluation;
import com.stocking.modules.buyornot.vo.StockChartRes;
import com.stocking.modules.buythen.repo.QStocksPrice;
import com.stocking.modules.firebase.QFireUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuyOrNotService {

    private final EvaluateBuySellRepository evaluateBuySellRepository;
    
    private final JPAQueryFactory queryFactory;
    
    private static final String LIKECOUNT = "likeCount";
    
    private final StockUtils stockUtils;
    
    /**
     * 전체 평가 목록 조회
     * @param order
     * @param pageSize
     * @param pageNo
     * @param searchWord
     * @return
     */
    public BuyOrNotRes getBuyOrNotList(FirebaseUser user, BuyOrNotOrder order, long pageSize, long pageNo, String searchWord) {
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        BooleanBuilder builder = new BooleanBuilder();
        Optional.ofNullable(searchWord)
            .ifPresent(vo -> builder.and(qEvaluate.company.contains(vo)));
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = switch (order) {
            case LATELY -> qEvaluate.id.desc();
            case POPULARITY -> aliasLikeCount.desc();
        };
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        }
        
        List<SimpleEvaluation> simpleEvaluationList = queryFactory.select(
                Projections.fields(SimpleEvaluation.class,
                    qEvaluate.id,
                    qEvaluate.code,
                    qEvaluate.company,
                    qEvaluate.pros,
                    qEvaluate.cons,
                    qEvaluate.createdUid.as("uid"),
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id.count())
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                        aliasLikeCount),   // 좋아요 횟수
                    userLike,
                    qEvaluate.createdDate,
                    qFireUser.displayName
                )
            ).from(qEvaluate)
            .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
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
    public EvaluationRes getEvaluationList(FirebaseUser user, String stockCode, BuyOrNotOrder order, long pageSize, long pageNo) {
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        // 정렬조건
        OrderSpecifier<?> orderSpecifier = switch (order) {
            case LATELY -> qEvaluate.id.desc();
            case POPULARITY -> aliasLikeCount.desc();
        };
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        }
        
        List<Evaluation> evaluationList = queryFactory.select(
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
                userLike,    // 사용자가 좋아요했는지 여부
                qEvaluate.createdDate,
                qFireUser.displayName
            )
        ).from(qEvaluate)
        .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
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
                            qEvaluateComment.createdUid.as("uid"),
                            qFireUser.displayName
                            )
                        )
                    .from(qEvaluateComment)
                    .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluateComment.createdUid))
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
    public SimpleEvaluation getTodayBest(FirebaseUser user) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QFireUser qFireUser = QFireUser.fireUser;
        
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
        
        if(tuple == null) { // 오늘 좋아요를 받은 평가 없는 경우, 전체기간으로 조회
            tuple = queryFactory.select(
                qEvaluateLike.evaluateId,
                qEvaluateLike.id.count().as(groupCount)
            )
            .from(qEvaluateLike)
//            .where(qEvaluateLike.createdDate.between(now.atTime(0, 0, 0), now.atTime(23, 59, 59)))
            .groupBy(qEvaluateLike.evaluateId)
            .orderBy(groupCount.desc())
            .limit(1)
            .fetchOne();
        }
        
        Long evaluateId = tuple.get(qEvaluateLike.evaluateId);
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        }
        
        return queryFactory.select(
              Projections.fields(SimpleEvaluation.class,
                  qEvaluate.id,
                  qEvaluate.code,
                  qEvaluate.company,
                  qEvaluate.pros,
                  qEvaluate.cons,
                  qEvaluate.createdUid.as("uid"),
                  ExpressionUtils.as(
                      JPAExpressions.select(qEvaluateLike.id.count())
                          .from(qEvaluateLike)
                          .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                      aliasLikeCount),   // 좋아요 횟수
                  userLike,
                  qEvaluate.createdDate,
                  qFireUser.displayName
              )
          ).from(qEvaluate)
          .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
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
    public Map<String, Object> saveBuySell(String stockCode, String uid, BuySell buySell) {
        Map<String, Object> resultMap = new HashMap<>();
        
        if(buySell != null) {
            evaluateBuySellRepository.findByCodeAndUid(stockCode, uid).ifPresentOrElse(vo -> {
                vo.setBuySell(buySell);
                evaluateBuySellRepository.save(vo);
                resultMap.put("id", vo.getId());
            }, () -> {
                EvaluateBuySell evaluateBuySell = EvaluateBuySell.builder()
                        .uid(uid)
                        .buySell(buySell)
                        .code(stockCode)
                        .build();
                
                evaluateBuySellRepository.save(evaluateBuySell);
                resultMap.put("id", evaluateBuySell.getId());
            });
        }else {
            evaluateBuySellRepository.findByCodeAndUid(stockCode, uid)
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
    public EvaluateBuySellRes getBuySellCount(String stockCode, String uid){
        long buyCount = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.BUY);
        long sellCount = evaluateBuySellRepository.countByCodeAndBuySell(stockCode, BuySell.SELL);
        
        BuySell buySell = null;
        
        EvaluateBuySell evaluateBuySell = evaluateBuySellRepository
                .findByCodeAndUid(stockCode, uid).orElse(null);
        
        if(evaluateBuySell != null) {
            buySell = evaluateBuySell.getBuySell();
        }
        
        return EvaluateBuySellRes.builder()
            .code(stockCode)
            .buyCount(buyCount)
            .sellCount(sellCount)
            .userBuySell(buySell)
            .build();
    }
    
    /**
     * 종목별 기간별 좋아요를 많이 받은 평가 목록
     * @param accountId
     * @param stockCode
     * @param period
     * @param pageSize
     * @param pageNo
     * @return
     */
    public EvaluationRes getBestEvaluationList(FirebaseUser user, String stockCode, BuyOrNotPeriod period, long pageSize, long pageNo) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startDt = now.atTime(0, 0, 0);
        LocalDateTime endDt = now.atTime(23, 59, 59);
        
        // q class
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QEvaluateComment qEvaluateComment = QEvaluateComment.evaluateComment;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        BooleanExpression booleanExpression = switch (period) {
            case MONTH12 -> qEvaluateLike.createdDate.between(startDt.minusMonths(12), endDt);
            case MONTH6 -> qEvaluateLike.createdDate.between(startDt.minusMonths(6), endDt);
            case MONTH1 -> qEvaluateLike.createdDate.between(startDt.minusMonths(1), endDt);
            case WEEK -> qEvaluateLike.createdDate.between(startDt.minusWeeks(1), endDt);
            case TODAY -> qEvaluateLike.createdDate.between(startDt, endDt);
        };
        
        BooleanBuilder whereClause = new BooleanBuilder();
        
        whereClause.and(qEvaluateLike.evaluateId.eq(qEvaluate.id));
        
//        Optional.ofNullable(booleanExpression).ifPresent(whereClause::and);   // 기간내 좋아요 수 조건, 항시 전체 좋아요수 보여주기위해 주석
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        
        }
        
        List<Evaluation> evaluationList = queryFactory.select(
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
                        .where(whereClause),
                    aliasLikeCount),   // 좋아요 횟수
                userLike,     // 사용자가 좋아요했는지 여부
                qEvaluate.createdDate,
                qFireUser.displayName
            )
        ).from(qEvaluate)
        .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
        .where(
            qEvaluate.code.eq(stockCode)
            .and(qEvaluate.id.in(JPAExpressions.select(qEvaluateLike.evaluateId)
                        .from(qEvaluateLike)
                        .where(booleanExpression)))
        )
        .orderBy(aliasLikeCount.desc())
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
                            qEvaluateComment.createdUid.as("uid"),
                            qEvaluateComment.createdDate,
                            qFireUser.displayName
                            )
                        )
                    .from(qEvaluateComment)
                    .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluateComment.createdUid))
                    .where(qEvaluateComment.evaluateId.eq(vo.getId()))
                    .orderBy(qEvaluateComment.createdDate.desc())
                    .limit(1)
                    .fetchOne();
                vo.setRecentComment(comment);
            }
            return vo;
        }).collect(Collectors.toList());
        
        // 전체 건수
        long count = queryFactory.selectFrom(qEvaluate).where(
                qEvaluate.code.eq(stockCode)
                    .and(qEvaluate.id.in(
                        JPAExpressions.select(qEvaluateLike.id)
                            .from(qEvaluateLike)
                            .where(booleanExpression)))
            ).fetchCount();
        
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
     * 그래프 데이터 목록
     * @param user
     * @param stockCodeList
     * @return
     */
    public List<StockChartRes> getStockChartList(FirebaseUser user, List<String> stockCodeList) {
        List<StockChartRes> resultList = new ArrayList<>();
        
        for (String stockCode : stockCodeList) {
            resultList.add(getStockChart(user, stockCode));
        }
        
        return resultList;
    }
    
    /**
     * 그래프 데이터임. (수정예정 - 캐시 처리하여) 
     * @param stockCode
     */
    public StockChartRes getStockChart(FirebaseUser user, String stockCode) {
        
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(qEvaluateLike.evaluateId.eq(qEvaluate.id));
        
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
                        .where(whereClause),
                aliasLikeCount),   // 좋아요 횟수
                userLike,
                qEvaluate.createdDate,
                qFireUser.displayName
            )
        ).from(qEvaluate)
        .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
        .where(qEvaluate.code.eq(stockCode))
        .orderBy(aliasLikeCount.desc())
        .limit(1)
        .fetchOne();
        
        return StockChartRes.builder()
                .evaluation(evaluation)
                .stockHist(stockUtils.getStockHist(stockCode))
                .build();
    }
    
    /**
     * 살래 갯수로 정렬하여 순위 목록 출력(list type - simple(3), detail(10))
     * @param buySell
     * @param rankListType
     * @return
     */
    public BuySellRankRes getBuyRankList(FirebaseUser user, BuySell buySell, RankListType rankListType) {
        QEvaluateBuySell qEvaluateBuySell = QEvaluateBuySell.evaluateBuySell;
        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        QEvaluate qEvaluate = QEvaluate.evaluate;
        QEvaluateLike qEvaluateLike = QEvaluateLike.evaluateLike;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberExpression<Long> buyCnt = qEvaluateBuySell.buySell
            .when(BuySell.BUY).then(1L).otherwise(0L);
        
        NumberExpression<Long> sellCnt = qEvaluateBuySell.buySell
            .when(BuySell.SELL).then(1L).otherwise(0L);
        
        NumberPath<Long> buyCntPath = Expressions.numberPath(Long.class, "buyCnt");
        NumberPath<Long> sellCntPath = Expressions.numberPath(Long.class, "sellCnt");
        
        OrderSpecifier<?>[] orderSpecifierList = switch (buySell) {
            case BUY -> new OrderSpecifier<?>[] {buyCntPath.desc(), sellCntPath.asc()};
            case SELL -> new OrderSpecifier<?>[] {sellCntPath.desc(), buyCntPath.asc()};
            default -> throw new IllegalArgumentException("Unexpected value: " + buySell);
        };
        
        List<GroupResult> groupResultList = queryFactory.select(
                    Projections.fields(GroupResult.class,
                        qEvaluateBuySell.code,
                        ExpressionUtils.as(
                                JPAExpressions.select(qStocksPrice.company)
                                    .from(qStocksPrice)
                                    .where(qStocksPrice.code.eq(qEvaluateBuySell.code)),
                                "company"),
                        ExpressionUtils.as(
                                JPAExpressions.select(qStocksPrice.sectorYahoo)
                                    .from(qStocksPrice)
                                    .where(qStocksPrice.code.eq(qEvaluateBuySell.code)),
                                "sectorYahoo"),
                        buyCnt.sum().as(buyCntPath),
                        sellCnt.sum().as(sellCntPath)
                    )    
                )
                .from(qEvaluateBuySell)
                .groupBy(qEvaluateBuySell.code)
                .orderBy(orderSpecifierList)
                .limit(rankListType.getLimit())
                .fetch();
        
        groupResultList.forEach(vo -> {
            try {
                RealTimeStock realTimeStock = stockUtils.getStockInfo(vo.getCode());
                vo.setCurrentPrice(realTimeStock.getCurrentPrice());
                vo.setChange(realTimeStock.getChange());
                vo.setChangeInPercent(realTimeStock.getChangeInPercent());
            } catch (IOException e) {
                log.error("stockUtils.getStockInfo 에러 발생", e);;
            }
        });
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, LIKECOUNT);
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qEvaluateLike.id)
                    .from(qEvaluateLike)
                    .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)
                            .and(qEvaluateLike.uid.eq(user.getUid()))).exists(),
                "userlike");     // 사용자가 좋아요했는지 여부
        }
        
        SimpleEvaluation simpleEvaluation = Optional.ofNullable(queryFactory.select(
                Projections.fields(SimpleEvaluation.class,
                    qEvaluate.id,
                    qEvaluate.code,
                    qEvaluate.company,
                    qEvaluate.pros,
                    qEvaluate.cons,
                    qEvaluate.createdUid.as("uid"),
                    ExpressionUtils.as(
                        JPAExpressions.select(qEvaluateLike.id.count())
                            .from(qEvaluateLike)
                            .where(qEvaluateLike.evaluateId.eq(qEvaluate.id)),
                        aliasLikeCount),   // 좋아요 횟수
                    userLike,
                    qEvaluate.createdDate,
                    qFireUser.displayName
                )
            ).from(qEvaluate)
            .leftJoin(qFireUser).on(qFireUser.uid.eq(qEvaluate.createdUid))
            .where(qEvaluate.code.eq(groupResultList.get(0).getCode()))
            .orderBy(aliasLikeCount.desc())
            .limit(1)
            .fetchOne()).orElse(SimpleEvaluation.builder().build());
        
        return BuySellRankRes.builder()
            .groupResultList(groupResultList)
            .rankTop(
                RankTop.builder()
                    .pros(simpleEvaluation.getPros())
                    .cons(simpleEvaluation.getCons())
                    .createdDate(simpleEvaluation.getCreatedDate())
                    .displayName(simpleEvaluation.getDisplayName())
                    .build()
            ).build();
    }

}
