package com.stocking.modules.buythen;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
public class BuyThenException extends Exception {

    // 예외 케이스 종류(
    private ExceptionType exceptionType;

    // 기간값 오류
    private InvestDate givenDate;
    private InvestDate correctDate;

    // 금액값 오류
    private BigDecimal givenPrice;
    private BigDecimal correctPrice;
}
