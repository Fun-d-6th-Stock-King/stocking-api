package com.stocking.modules.buythen;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CalcHighestRes {

    @ApiModelProperty(notes = "종목 코드", required=false, position=1)
    private String code;
    @ApiModelProperty(notes = "투자시기", required=false, position=1)
    private String investDate;
    @ApiModelProperty(notes = "투자금", required=false, position=2)
    private BigDecimal investPrice;
    
    @ApiModelProperty(notes = "수익금", required=false, position=3)
    private BigDecimal yieldPrice;
    @ApiModelProperty(notes = "수익률", required=false, position=4)
    private BigDecimal yieldPercent;
    @ApiModelProperty(notes = "투자시기 일자", required=false, position=5)
    private LocalDateTime investStartDate;
    @ApiModelProperty(notes = "최고가 일자", required=false, position=6)
    private LocalDateTime investEndDate;
    @ApiModelProperty(notes = "투자기간", required=false, position=7)
    private String investPeriod;
    
}