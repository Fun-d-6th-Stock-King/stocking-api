package com.stocking.modules.buyornot.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.stocking.infra.common.PageInfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyOrNotRes {
    
    @ApiModelProperty(notes = "페이지정보", required=false, position=1)
    private PageInfo pageInfo;

    @ApiModelProperty(notes = "평가목록", required=false, position=2)
    private List<SimpleEvaluation> simpleEvaluationList;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SimpleEvaluation {
        
//        private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
        
        private long id;
        private String code;        // 종목코드
        private String company;     // 회사명
        private String pros;        // 장점
        private String cons;        // 단점
        private String uid;         // 등록자 uid
        private long likeCount;     // 좋아요 개수
        private boolean userlike;   // 사용자가 좋아요 했는지 여부
        private LocalDateTime createdDate;       // 작성일시
        private String displayName; // 등록자 이름
        
//        public String getCreatedDate() {
//            return this.createdDate.format(DateTimeFormatter.ofPattern(FORMAT));
//        }
    }

}
