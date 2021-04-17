package com.stocking.modules.todayword;

import org.springframework.stereotype.Service;

import com.stocking.infra.common.FirebaseUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodayWordService {
    
    private final TodayWordRepository todayWordRepository;
    
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
}
