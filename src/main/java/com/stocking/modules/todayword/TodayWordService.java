package com.stocking.modules.todayword;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.common.PageInfo;
import com.stocking.modules.firebase.QFireUser;
import com.stocking.modules.todayword.repo.QTodayWord;
import com.stocking.modules.todayword.repo.QTodayWordLike;
import com.stocking.modules.todayword.repo.TodayWord;
import com.stocking.modules.todayword.repo.TodayWordLike;
import com.stocking.modules.todayword.repo.TodayWordLikeRepository;
import com.stocking.modules.todayword.repo.TodayWordRepository;
import com.stocking.modules.todayword.vo.TodayWordOrder;
import com.stocking.modules.todayword.vo.TodayWordReq;
import com.stocking.modules.todayword.vo.TodayWordRes;
import com.stocking.modules.todayword.vo.TodayWordSortRes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodayWordService {

    private final TodayWordRepository todayWordRepository;
    
    private final TodayWordLikeRepository todayWordLikeRepository;
    
    private final JPAQueryFactory queryFactory;

    /**
     * 단어 저장
     * @param user
     * @param todayWordReq
     * @return
     */
    public TodayWord saveTodayWord(FirebaseUser user, TodayWordReq todayWordReq) {
        
        return todayWordRepository.save(
                TodayWord.builder()
                    .wordName(todayWordReq.getWordName())
                    .mean(todayWordReq.getMean())
                    .createdUid(user.getUid())
                    .build()
            );
    }
    
    /**
     * 좋아요/안좋아요 (존재여부 체크함)
     * @param user
     * @param todayWordId
     * @return
     */
    public TodayWordRes saveTodayWordLike(FirebaseUser user, Long todayWordId) {
        
        todayWordLikeRepository.findByTodayWordIdAndCreatedUid(todayWordId, user.getUid())
            .ifPresentOrElse(vo -> 
                todayWordLikeRepository.delete(vo)
            , () -> 
                todayWordLikeRepository.save(
                    TodayWordLike.builder()
                        .todayWordId(todayWordId)
                        .createdUid(user.getUid())
                        .build()
                ));
        
        return this.getTodayWord(user, todayWordId);
    }

    /**
     * 좋아요가 가장많은 오늘의 단어 (사용자 정보 넘어오면 사용자가 좋아요했는지도 확인해줌)
     * @param user
     * @param todayWordId
     * @return
     */
    public TodayWordRes getTopWord(FirebaseUser user) {
        QTodayWord qTodayWord = QTodayWord.todayWord;
        QTodayWordLike qTodayWordLike = QTodayWordLike.todayWordLike;
        QFireUser qFireUser = QFireUser.fireUser;
        
        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");
        
        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                JPAExpressions.select(qTodayWordLike.id)
                .from(qTodayWordLike)
                .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)
                        .and(qTodayWordLike.createdUid.eq(user.getUid()))).exists(),
                "userlike");
        }
        
        return queryFactory.select(
                Projections.fields(TodayWordRes.class,
                    qTodayWord.id,
                    qTodayWord.wordName,
                    qTodayWord.mean,
                    qTodayWord.createdUid,
                    qTodayWord.createdDate,
                    ExpressionUtils.as(
                        JPAExpressions.select(qTodayWordLike.id.count())
                            .from(qTodayWordLike)
                            .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)),
                        aliasLikeCount),   // 좋아요 횟수
                    userLike,    // 사용자가 좋아요했는지 여부
                    qFireUser.displayName
                )
            ).from(qTodayWord)
            .leftJoin(qFireUser).on(qFireUser.uid.eq(qTodayWord.createdUid))
            .orderBy(aliasLikeCount.desc())
            .limit(1)
            .fetchOne();
        
    }

    /**
     * 오늘의 단어 조회
     * @param user
     * @param todayWordId
     * @return
     */
    public TodayWordRes getTodayWord(FirebaseUser user, Long todayWordId) {
        QTodayWord qTodayWord = QTodayWord.todayWord;
        QTodayWordLike qTodayWordLike = QTodayWordLike.todayWordLike;
        QFireUser qFireUser = QFireUser.fireUser;

        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");

        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if(user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qTodayWordLike.id)
                            .from(qTodayWordLike)
                            .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)
                                    .and(qTodayWordLike.createdUid.eq(user.getUid()))).exists(),
                    "userlike");
        }

        return queryFactory.select(
                Projections.fields(TodayWordRes.class,
                        qTodayWord.id,
                        qTodayWord.wordName,
                        qTodayWord.mean,
                        qTodayWord.createdUid,
                        qTodayWord.createdDate,
                        ExpressionUtils.as(
                                JPAExpressions.select(qTodayWordLike.id.count())
                                        .from(qTodayWordLike)
                                        .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)),
                                aliasLikeCount),   // 좋아요 횟수
                        userLike,    // 사용자가 좋아요했는지 여부
                        qFireUser.displayName
                    )
                ).from(qTodayWord)
                .leftJoin(qFireUser).on(qFireUser.uid.eq(qTodayWord.createdUid))
                .where(qTodayWord.id.eq(todayWordId))
                .fetchOne();
    }

    /**
     * 등록되어 있는 오늘의 단어 수정
     * @param user
     * @param todayWordReq
     * @param todayWordId
     * @return
     */
    public Long updateTodayWord(FirebaseUser user, TodayWordReq todayWordReq, Long todayWordId) {

        todayWordRepository.findByIdAndCreatedUid(todayWordId, user.getUid())
                .ifPresent(vo -> {
                    todayWordRepository.save(TodayWord.builder()
                            .id(todayWordId)
                            .wordName(todayWordReq.getWordName())
                            .mean(todayWordReq.getMean())
                            .createdUid(user.getUid())
                            .build());
                });

        return todayWordId;
    }


    /**
     * 조회 기준에 따른 오늘의 단어 목록 조회
     * @param TodayWordOrder
     * @param pageSize
     * @param pageNo
     * @return
     */
    public TodayWordSortRes getTodayWordSortList(FirebaseUser user, TodayWordOrder order, int pageSize, int pageNo) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startDt = now.atTime(0, 0, 0);
        LocalDateTime endDt = now.atTime(23, 59, 59);

        QTodayWord qTodayWord = QTodayWord.todayWord;
        QTodayWordLike qTodayWordLike = QTodayWordLike.todayWordLike;
        QFireUser qFireUser = QFireUser.fireUser;

        NumberPath<Long> aliasLikeCount = Expressions.numberPath(Long.class, "likeCount");

        Expression<Boolean> userLike = ExpressionUtils.as(Expressions.FALSE, "userlike");
        if (user.getUid() != null) {
            userLike = ExpressionUtils.as(
                    JPAExpressions.select(qTodayWordLike.id)
                            .from(qTodayWordLike)
                            .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)
                                    .and(qTodayWordLike.createdUid.eq(user.getUid()))).exists(),
                    "userlike");
        }

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = switch (order) {
            case LATELY -> qTodayWord.id.desc();
            case POPULARITY -> aliasLikeCount.desc();
            case WEEKLY_POPULARITY -> aliasLikeCount.desc();
        };

        List<TodayWordRes> todayWordSortResList;
        if (order == TodayWordOrder.WEEKLY_POPULARITY) {
            todayWordSortResList = queryFactory.select(
                    Projections.fields(TodayWordRes.class,
                            qTodayWord.id,
                            qTodayWord.wordName,
                            qTodayWord.mean,
                            qTodayWord.createdUid,
                            qTodayWord.createdDate,
                            ExpressionUtils.as(
                                    JPAExpressions.select(qTodayWordLike.id.count())
                                            .from(qTodayWordLike)
                                            .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)),
                                    aliasLikeCount),
                            userLike,
                            qFireUser.displayName
                    )
            ).from(qTodayWord)
                    .leftJoin(qFireUser).on(qFireUser.uid.eq(qTodayWord.createdUid))
                    .orderBy(orderSpecifier, qTodayWord.wordName.desc())
                    .offset((pageNo - 1) * pageSize)
                    .where(qTodayWord.createdDate.between(startDt.minusWeeks(1), endDt))
                    .limit(pageSize)
                    .fetch();
        } else {
            todayWordSortResList = queryFactory.select(
                    Projections.fields(TodayWordRes.class,
                            qTodayWord.id,
                            qTodayWord.wordName,
                            qTodayWord.mean,
                            qTodayWord.createdUid,
                            qTodayWord.createdDate,
                            ExpressionUtils.as(
                                    JPAExpressions.select(qTodayWordLike.id.count())
                                            .from(qTodayWordLike)
                                            .where(qTodayWordLike.todayWordId.eq(qTodayWord.id)),
                                    aliasLikeCount),
                            userLike,
                            qFireUser.displayName
                    )
            ).from(qTodayWord)
                    .leftJoin(qFireUser).on(qFireUser.uid.eq(qTodayWord.createdUid))
                    .orderBy(orderSpecifier, qTodayWord.wordName.desc())
                    .offset((pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .fetch();
        }

        long count = queryFactory.selectFrom(qTodayWord).fetchCount();

        return TodayWordSortRes.builder()
            .todayWordResList(todayWordSortResList)
            .pageInfo(
                PageInfo.builder()
                    .pageSize(pageSize)
                    .pageNo(pageNo)
                    .count(count)
                    .build()
            ).build();
    }
}
