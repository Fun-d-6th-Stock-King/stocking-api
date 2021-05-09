package com.stocking.modules.todayword;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.buyornot.repo.EvaluateLike;
import com.stocking.modules.buyornot.repo.EvaluateLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodayWordService {

//    @Autowired
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
    public Map<String, Object> saveTodayWordLike(FirebaseUser user, Long todayWordId) {
        Map<String, Object> resultMap = new HashMap<>();
        
        todayWordLikeRepository.findByTodayWordIdAndCreatedUid(todayWordId, user.getUid())
            .ifPresentOrElse(vo -> {
                todayWordLikeRepository.delete(vo);
                resultMap.put("result", "unlike");
            }, () -> {
                todayWordLikeRepository.save(
                    TodayWordLike.builder()
                        .todayWordId(todayWordId)
                        .createdUid(user.getUid())
                        .build()
                );
                resultMap.put("result", "like");
            });
        
        return resultMap;
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
                    userLike    // 사용자가 좋아요했는지 여부
                )
            ).from(qTodayWord)
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
                        userLike    // 사용자가 좋아요했는지 여부
                )
        ).from(qTodayWord)
                .where(qTodayWord.id.eq(todayWordId))
                .orderBy(aliasLikeCount.desc())
                .limit(1)
                .fetchOne();
    }

    /**
     * 등록되어 있는 오늘의 단어 수정
     * @param user
     * @param todayWordReq
     * @return
     */
    public void updateTodayWord(FirebaseUser user, TodayWordReq todayWordReq) {

    }


    /**
     * 최근 기준으로 등록된 오늘의 단어 목록
     * @param pageParam
     * @return
     */
    public TodayWordSortRes getRecentlyTodayWordSortList(int pageSize, int pageNo) {
        return null;
    }

}
