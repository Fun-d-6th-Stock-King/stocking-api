package com.stocking.modules.buyornot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateReq {

    private String code;
    private String pros;
    private String cons;
    private String giphyImgId;

}
