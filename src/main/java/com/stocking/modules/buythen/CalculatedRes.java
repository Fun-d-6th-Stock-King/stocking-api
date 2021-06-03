package com.stocking.modules.buythen;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CalculatedRes {

    @ApiModelProperty(notes = "종목 코드", required=false, position=1)
    private String code;    
    @ApiModelProperty(notes = "회사명", required=false, position=2)
    private String company; 
    @ApiModelProperty(notes = "현재 주가", required=false, position=3)
    private BigDecimal currentPrice; 
    @ApiModelProperty(notes = "마지막 거래 일시", required=false, position=4)
    private String lastTradingDateTime;
    
    @ApiModelProperty(notes = "계산 결과", required=false, position=5)
    private CalculatedValue calculatedValue;
    @ApiModelProperty(notes = "예외 케이스", required=false, position=6)
    private ExceptCase exceptCase;
    
    @Data
    @AllArgsConstructor
    @Builder
    public static class CalculatedValue {
        @ApiModelProperty(notes = "투자시기", required=false, position=1)
        private String investDate;
        @ApiModelProperty(notes = "투자금", required=false, position=2)
        private BigDecimal investPrice;
        @ApiModelProperty(notes = "수익금", required=false, position=3)
        private BigDecimal yieldPrice;
        @ApiModelProperty(notes = "수익률", required=false, position=4)
        private BigDecimal yieldPercent;
        @ApiModelProperty(notes = "종가일자", required=false, position=5)
        private String oldCloseDate;
        @ApiModelProperty(notes = "그때 주가", required=false, position=6)
        private BigDecimal oldPrice;
        @ApiModelProperty(notes = "보유 주식 수", required=false, position=7)
        private BigDecimal holdingStock;
        @ApiModelProperty(notes = "연봉", required=false, position=8)
        private BigDecimal salaryYear;
        @ApiModelProperty(notes = "월급", required=false, position=9)
        private BigDecimal salaryMonth;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ExceptCase {
        @ApiModelProperty(notes = "예외 케이스 여부", required=false, position=1)
        private Boolean isExceptCase;

        @ApiModelProperty(notes = "기간값 예외 케이스 여부", required=false, position=2)
        private Boolean isDateExcept;
        @ApiModelProperty(notes = "기존 투자 날짜", required=false, position=3)
        private InvestDate oldInvestDate;
        @ApiModelProperty(notes = "새 투자 날짜", required=false, position=4)
        private InvestDate newInvestDate;

        @ApiModelProperty(notes = "금액값 예외 케이스 여부", required=false, position=5)
        private Boolean isPriceExcept;
        @ApiModelProperty(notes = "기존 금액값", required=false, position=6)
        private BigDecimal oldInvestPrice;
        @ApiModelProperty(notes = "새 금액값", required=false, position=7)
        private BigDecimal newInvestPrice;

        @ApiModelProperty(notes = "종목 예외 케이스 여부", required=false, position=8)
        private Boolean isStockExcept;
        @ApiModelProperty(notes = "거래중지 여부", required=false, position=9)
        private Boolean isTradingHalt;
        @ApiModelProperty(notes = "투자경고 여부", required=false, position=10)
        private Boolean isInvestmentAlert;
        @ApiModelProperty(notes = "관리종목(상장폐지 우려) 여부", required=false, position=11)
        private Boolean isManagement;
    }
}