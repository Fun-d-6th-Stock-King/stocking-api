package com.stocking.modules.buythen;

import java.math.BigDecimal;
import java.math.MathContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuyThenService {

    @Autowired
    private OldStockRepository oldStockRepository;
    
    /**
     * 계산기
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public BuyThen getPastStock(BuyThenForm buyThenForm) throws Exception {
        OldStock oldStock = oldStockRepository.findByCode(buyThenForm.getCode())
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));
        String code = oldStock.getCode();
        InvestDate date = buyThenForm.getDate();
        BigDecimal investPrice = buyThenForm.getInvestPrice();    // 투자금

        // 과거 주가
        BigDecimal oldStockPrice = new BigDecimal(0);
        if (date == InvestDate.YEAR1) { // 1년전
            oldStockPrice = oldStock.getOneAgoStock();
        } else if (date == InvestDate.YEAR5) {  // 5년전
            oldStockPrice = oldStock.getFiveAgoStock();
        } else if (date == InvestDate.YEAR10) { // 10년전
            oldStockPrice = oldStock.getTenAgoStock();
        }
        
        BigDecimal currentPrice = oldStock.getCurrentStock();
        BigDecimal holdingStock = investPrice.divide(oldStockPrice, MathContext.DECIMAL32);     // 내가 산 주식 개수 
        BigDecimal yieldPrice = holdingStock.multiply(currentPrice); // 수익금 = (투자금/이전종가) * 현재가
        BigDecimal yieldPercent = currentPrice.subtract(oldStockPrice).divide(oldStockPrice, MathContext.DECIMAL32)
                .multiply(new BigDecimal(100));  // (현재가-이전종가)/이전종가 * 100

        BigDecimal salaryYear = new BigDecimal(0);      // 연봉
        BigDecimal salaryMonth = new BigDecimal(0);     // 월급
        if (date == InvestDate.YEAR1) { // 1년전
            salaryYear = yieldPrice;
            salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
        } else if (date == InvestDate.YEAR5) {  // 5년전
            salaryYear = yieldPrice.divide(new BigDecimal(5), MathContext.DECIMAL32);
            salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
        } else if (date == InvestDate.YEAR10) { // 10년전
            salaryYear = yieldPrice.divide(new BigDecimal(10), MathContext.DECIMAL32);
            salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
        }
        
        return BuyThen.builder()
            .code(code)
            .company(oldStock.getCompany())
            .date(date)
            .oldPrice(oldStockPrice)
            .currentPrice(currentPrice)
            .yieldPrice(yieldPrice)
            .yieldPercent(yieldPercent)
            .holdingStock(holdingStock)
            .salaryYear(salaryYear)
            .salaryMonth(salaryMonth)
            .build();
    }
}