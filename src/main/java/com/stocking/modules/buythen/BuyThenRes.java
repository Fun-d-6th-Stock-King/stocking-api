package com.stocking.modules.buythen;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BuyThenRes {

    private String code;    // 종목 코드
    private String company; // 회사명
    private InvestDate date;    // 날짜 ex) 2020-12-31
    private BigDecimal oldPrice;     // 그때의 주가
    private BigDecimal currentPrice;     // 직전 주가
    private BigDecimal yieldPrice;          // 수익금
    private BigDecimal yieldPercent;     // 수익률
    private BigDecimal holdingStock;     // 보유 주식 수
    
    private BigDecimal salaryYear;      // 연봉
    private BigDecimal salaryMonth;     // 월급
    
}