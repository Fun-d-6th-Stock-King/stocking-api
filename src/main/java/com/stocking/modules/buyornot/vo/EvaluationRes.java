package com.stocking.modules.buyornot.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.stocking.infra.common.PageInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationRes {

    private List<Evaluation> evaluationList;
    
    private PageInfo pageInfo;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Evaluation {
        
        private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
        
        private long id;
        private String code;        // 종목코드
        private String company;     // 회사명
        private String pros;        // 장점
        private String cons;        // 단점
        private String giphyImgId;  // giphy 이미지
        private String uid;        // 등록자 uid
        private long likeCount;     // 좋아요 개수
        private boolean userlike;   // 사용자가 좋아요 했는지 여부
        private LocalDateTime createdDate;       // 작성일시
        
        public String getCreatedDate() {
            return this.createdDate.format(DateTimeFormatter.ofPattern(FORMAT));
        }
        
        private Comment recentComment;  // comment 최근
        private long commentCount;  // comment 갯수
        
    }

}
