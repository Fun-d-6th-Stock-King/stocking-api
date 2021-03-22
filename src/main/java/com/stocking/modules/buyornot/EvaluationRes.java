package com.stocking.modules.buyornot;

import java.util.List;

import com.stocking.infra.common.PageInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationRes {

    private List<Evaluation> evaluationList;
    
    private PageInfo pageInfo;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Evaluation {
        
        private int id;
        private String code;        // 종목코드
        private String company;     // 회사명
        private String pros;        // 장점
        private String cons;        // 단점
        private String giphyImgId;  // giphy 이미지
        private String uuid;        // 등록자 uid
        private long likeCount;     // 좋아요 개수
        private boolean userlike;   // 사용자가 좋아요 했는지 여부
        
        private Comment recentComment;  // comment 최근
        private long commentCount;  // comment 갯수
        
    }

}
