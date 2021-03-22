package com.stocking.modules.buyornot;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateReq {

    @ApiModelProperty(notes = "종목코드", position = 1)
    private String code;
    
    @ApiModelProperty(notes = "장점", position = 2)
    private String pros;
    
    @ApiModelProperty(notes = "단점", position = 3)
    private String cons;
    
    @ApiModelProperty(notes = "giphy 이미지 id", position = 4)
    private String giphyImgId;

}
