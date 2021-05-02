package com.stocking.modules.buyornot.vo;

import com.stocking.infra.common.StockUtils.StockHist;
import com.stocking.modules.buyornot.vo.EvaluationRes.Evaluation;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StockChartRes {
    
    @ApiModelProperty(notes = "좋아요 가장많은 평가", required=false, position=1)
    private Evaluation evaluation;
    
    @ApiModelProperty(notes = "차트데이터", required=false, position=2)
    private StockHist stockHist;
    
}
