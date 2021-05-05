package com.stocking.infra.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

@Component
@Slf4j
public class StockUtils {

    /**
     * 종목코드를 받아서 현재가, 마지막거래일시를 실시간으로 받아옴(1시간단위캐시)
     * @param code
     * @return
     * @throws IOException 
     */
    @Cacheable(value = "stockCache", key = "#code")
    public RealTimeStock getStockInfo(String code) throws IOException{
        // kospi 일 때만 symbol 규칙이 변경됨
        Stock yahooStock = "KS11".equals(code) ? YahooFinance.get("^" + code) : YahooFinance.get(code + ".KS");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
        Date now = new Date();
        return RealTimeStock.builder()
                .currentPrice(yahooStock.getQuote().getPrice())
                .lastTradeTime(sdf.format(yahooStock.getQuote().getLastTradeTime().getTime()))
                .changeInPercent(yahooStock.getQuote().getChangeInPercent())
                .yearHigh(yahooStock.getQuote().getYearHigh())
                .changeFromYearHigh(yahooStock.getQuote().getChangeFromYearHigh())
                .changeFromYearHighInPercent(yahooStock.getQuote().getChangeFromYearHighInPercent())
                .currentTime(sdf.format(now))
                .build();
        
    }

    /**
     * 종목코드 배열을 받아서 현재가 총액 합산하여 반환
     * @param codes
     * @return
     * @throws IOException
     */
    public BigDecimal getCurrentSumPrice(String[] codes) throws IOException{
        Map<String, Stock> yahooStock = YahooFinance.get(codes);
        BigDecimal sumPrice = new BigDecimal(0);

        for (String key : yahooStock.keySet()) {
            BigDecimal price = yahooStock.get(key).getQuote().getPrice();
            sumPrice = sumPrice.add(price);
        }
        return sumPrice;
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class RealTimeStock{
        private BigDecimal currentPrice; // 현재 주가
        private String lastTradeTime;    // 최근 거래 일시
        private BigDecimal changeInPercent;
        private String currentTime;      // 현재가를 업데이트한 시간
        
        private BigDecimal yearHigh;    // 연중 최고가
        private BigDecimal changeFromYearHigh;  // 연중 최고가와 현재가 차이 금액
        private BigDecimal changeFromYearHighInPercent; // 연중 최고가와 현재가 차이비율
    }
    
    /**
     * 10년 기간내 최고 종가, 최소 종가 (주기-일주일) (1일 단위 캐시)
     * @param code
     * @return
     * @throws IOException
     */
    @Cacheable(value = "stockHistCache", key = "#code")
    public StockHist getStockHist(String code) {
        
        Stock yahooStock = null;
        List<HistoricalQuote> quoteList = null;
        
        Comparator<HistoricalQuote> comparatorByClose = 
                (x1, x2) -> (x1.getClose() == null || x2.getClose() == null) ? 0 :x1.getClose().compareTo(x2.getClose());
        
        try {
            yahooStock = "KS11".equals(code) ? YahooFinance.get("^" + code) : YahooFinance.get(code + ".KS");
            Calendar startDt = Calendar.getInstance();
            Calendar endDt = Calendar.getInstance();
            startDt.add(Calendar.YEAR, -10);
            quoteList = yahooStock.getHistory(startDt, endDt, Interval.WEEKLY);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        
        HistoricalQuote maxQuote = quoteList.stream().max(comparatorByClose)
                .orElseThrow(NoSuchElementException::new);
            
        HistoricalQuote minQuote = quoteList.stream().min(comparatorByClose)
            .orElseThrow(NoSuchElementException::new); 
        
        return StockHist.builder()
                .code(code)
                .price(yahooStock.getQuote().getPrice())                     // 현재 시세
                .changeInPercent(yahooStock.getQuote().getChangeInPercent()) // 등락률
                .maxQuote(maxQuote)
                .minQuote(minQuote)
                .quoteList(quoteList.stream().filter(vo -> vo.getClose() != null).collect(Collectors.toList()))
                .build();
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class StockHist{
        private String code;
        private BigDecimal price;
        private BigDecimal changeInPercent;
        private HistoricalQuote maxQuote;   // 10년 내 최고가 일자 정보
        private HistoricalQuote minQuote;
        private List<HistoricalQuote> quoteList;
    }
}
