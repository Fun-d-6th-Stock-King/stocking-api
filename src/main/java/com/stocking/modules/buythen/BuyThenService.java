package com.stocking.modules.buythen;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stocking.infra.common.StockUtils;
import com.stocking.infra.common.StockUtils.RealTimeStock;
import com.stocking.modules.buythen.BuyThenRes.CalculationResult;
import com.stocking.modules.buythen.StockRes.Company;
import com.stocking.modules.buythen.repo.StocksPrice;
import com.stocking.modules.buythen.repo.StocksPriceRepository;
import com.stocking.modules.stock.Stock;
import com.stocking.modules.stock.StockRepository;

@Service
public class BuyThenService {

    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private StocksPriceRepository stocksPriceRepository;
    
    @Autowired
    private StockUtils stockUtils;
    
    /**
     * kospi 상장기업 전체 조회
     * @return
     * @throws Exception 
     */
    public StockRes getStockList() throws Exception {
        
        List<Company> resultList = stockRepository.findAllByMarket("KS")
            .orElseThrow(() -> new Exception("조회 실패"))
            .stream().map(vo -> 
                Company.builder()
                    .company(vo.getCompany())
                    .code(vo.getCode())
                    .build()
            ).collect(Collectors.toList());
        
        return StockRes.builder()
            .companyList(resultList)
            .count(resultList.size())
            .build();
    }
    
    /**
     * 계산기
     * @param buyThenForm
     * @return
     * @throws Exception
     */
    public BuyThenRes getPastStock(BuyThenForm buyThenForm) throws Exception {
        BuyThenRes result;
        
        Stock stock = stockRepository.findByCode(buyThenForm.getCode())
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));
        
        StocksPrice stockPrice = stocksPriceRepository.findByStocksId(stock.getId())
                .orElseThrow(() -> new Exception("종목코드가 올바르지 않습니다."));
        
        String code = stock.getCode();
        InvestDate investDate = buyThenForm.getInvestDate();
        BigDecimal investPrice = buyThenForm.getInvestPrice();    // 투자금

        // 과거 주가
        BigDecimal oldStockPrice = Optional.ofNullable( switch (investDate) {
            case DAY1 -> stockPrice.getPrice();
            case WEEK1 -> stockPrice.getPriceW1();
            case MONTH1 -> stockPrice.getPriceM1();
            case MONTH6 -> stockPrice.getPriceM6();
            case YEAR1 -> stockPrice.getPriceY1();
            case YEAR5 -> stockPrice.getPriceY5();
            case YEAR10 -> stockPrice.getPriceY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        }).orElseThrow(() -> new Exception(stock.getCompany() + " 는(은) " + investDate.getName() + " 데이터가 없습니다."));
        
        // 종가일자
        LocalDateTime oldCloseDate = switch (investDate) {    
            case DAY1 -> stockPrice.getLastTradeDate();
            case WEEK1 -> stockPrice.getDateW1();
            case MONTH1 -> stockPrice.getDateM1();
            case MONTH6 -> stockPrice.getDateM6();
            case YEAR1 -> stockPrice.getDateY1();
            case YEAR5 -> stockPrice.getDateY5();
            case YEAR10 -> stockPrice.getDateY10();
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        RealTimeStock realTimeStock = stockUtils.getStockInfo(code);
        
        BigDecimal currentPrice = realTimeStock.getCurrentPrice(); // 현재가 - 실시간정보 호출
        String lastTradeTime = realTimeStock.getLastTradeTime();
        
        BigDecimal holdingStock = investPrice.divide(oldStockPrice, MathContext.DECIMAL32);     // 내가 산 주식 개수 
        BigDecimal yieldPrice = holdingStock.multiply(currentPrice); // 수익금 = (투자금/이전종가) * 현재가
        BigDecimal yieldPercent = currentPrice.subtract(oldStockPrice).divide(oldStockPrice, MathContext.DECIMAL32)
                .multiply(new BigDecimal(100));  // (현재가-이전종가)/이전종가 * 100

        BigDecimal salaryYear = new BigDecimal(0);      // 연봉
        BigDecimal salaryMonth = new BigDecimal(0);     // 월급
        
        switch (investDate) {
            case DAY1 -> {}
            case WEEK1 -> {}
            case MONTH1 -> {
                salaryMonth = yieldPrice;
            }
            case MONTH6 -> {
                salaryMonth = yieldPrice.divide(new BigDecimal(6), MathContext.DECIMAL32);
            }
            case YEAR1 -> {
                salaryYear = yieldPrice;
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
            }
            case YEAR5 -> {
                salaryYear = yieldPrice.divide(new BigDecimal(5), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
            }
            case YEAR10 -> {
                salaryYear = yieldPrice.divide(new BigDecimal(10), MathContext.DECIMAL32);
                salaryMonth = salaryYear.divide(new BigDecimal(12), MathContext.DECIMAL32);
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        result = BuyThenRes.builder()
            .code(code)
            .company(stock.getCompany())
            .currentPrice(currentPrice)
            .lastTradingDateTime(lastTradeTime)
            .calculationResult(
                CalculationResult.builder()
                    .investPrice(investPrice)
                    .investDate(investDate.getName())
                    .oldPrice(oldStockPrice)
                    .yieldPrice(yieldPrice)
                    .yieldPercent(yieldPercent)
                    .oldCloseDate(oldCloseDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .holdingStock(holdingStock)
                    .salaryYear(salaryYear)
                    .salaryMonth(salaryMonth)
                    .build()
            ).build();
        
        return result;
    }
}