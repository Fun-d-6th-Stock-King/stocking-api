package com.stocking.modules.todayword;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TodayWordReq {
        
    @ApiModelProperty(notes = "단어명", position = 1)
    private String wordName;

    @ApiModelProperty(notes = "단어의미", position = 2)
    private String mean;
        
}
