package com.stocking.modules.buythen;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import yahoofinance.histquotes.HistoricalQuote;

@Data
@AllArgsConstructor
@Builder
public class HighPriceRes {
	
    @ApiModelProperty(notes = "종목코드", required=false, position=2)
    private String code;

    @ApiModelProperty(notes = "회사명", required=false, position=3)
    private String company;
    
    @ApiModelProperty(notes = "분류", required=false, position=4)
    private String sector;
    
    @ApiModelProperty(notes = "분류 한글명", required=false, position=5)
    private String sectorKor;
    
    @ApiModelProperty(notes = "10년 내 최고가 일자 정보", required=false, position=7)
    private HistoricalQuote maxQuote;   
}
