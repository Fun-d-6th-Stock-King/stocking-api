package com.stocking.modules.buyornot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationDetailRes {
    
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; 

    private Evaluation evaluation;
    
    private List<CommentVO> commentList;
    
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
        
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentVO {
        
        private int id;
        private String comment;         // 코멘트
        private String uid;             // 작성자 uid
        private LocalDateTime createdDate;       // 작성일시
        
        public String getCreatedDate() {
            
            return this.createdDate.format(DateTimeFormatter.ofPattern(FORMAT));
        }
    }

}
