package com.stocking.modules.buyornot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Evaluation {
    
    private int id;
    private String code;        // 종목코드
    private String company;     // 회사명
    private String pros;        // 장점
    private String cors;        // 단점
    private String giphyImgId;  // giphy 이미지
    private long likeCount;     // 좋아요 개수
    private boolean userlike;
}
