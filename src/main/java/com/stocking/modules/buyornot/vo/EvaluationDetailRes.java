package com.stocking.modules.buyornot.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationDetailRes {
    
    private Evaluation evaluation;
    
    private List<Comment> commentList;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Evaluation {
        
        private long id;
        private String code;        // 종목코드
        private String company;     // 회사명
        private String pros;        // 장점
        private String cons;        // 단점
        private String giphyImgId;  // giphy 이미지
        private String uid;        // 등록자 uid
        private long likeCount;     // 좋아요 개수
        private boolean userlike;   // 사용자가 좋아요 했는지 여부
        
    }
    
}
