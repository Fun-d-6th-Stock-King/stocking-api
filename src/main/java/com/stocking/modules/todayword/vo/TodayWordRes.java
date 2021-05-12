package com.stocking.modules.todayword.vo;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class TodayWordRes {
    
    @ApiModelProperty(notes = "id", position = 1)
    private Long id;
    
    @ApiModelProperty(notes = "단어명", position = 2)
    private String wordName;

    @ApiModelProperty(notes = "단어의미", position = 3)
    private String mean;
    
    @ApiModelProperty(notes = "작성자 uid", position = 4)
    private String createdUid;
    
    @ApiModelProperty(notes = "작성일시", position = 5)
    private LocalDateTime createdDate;
    
    @ApiModelProperty(notes = "좋아요 갯수", position = 6)
    private long likeCount;
    
    @ApiModelProperty(notes = "사용자의 좋아요 여부", position = 7)
    private boolean userlike;
}
