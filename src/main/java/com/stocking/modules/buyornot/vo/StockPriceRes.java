package com.stocking.modules.buyornot.vo;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yahoofinance.histquotes.HistoricalQuote;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockPriceRes {
    
    @ApiModelProperty(notes = "현재시세", required=false, position=1)
    private BigDecimal price;
    
    @ApiModelProperty(notes = "등락률", required=false, position=2)
    private BigDecimal changeInPercent;
    
    @ApiModelProperty(notes = "종가 최대", required=false, position=3)
    private HistoricalQuote maxQuote;
    
    @ApiModelProperty(notes = "종가 최소", required=false, position=4)
    private HistoricalQuote minQuote;
    
    @ApiModelProperty(notes = "종가 목록", required=false, position=5)
    private List<HistoricalQuote> quoteList;
}
