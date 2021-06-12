package com.stocking.infra.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.stocking.modules.buythen.InvestDate;
import com.stocking.modules.stock.StockRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

@Component
@Slf4j
public class StockUtils {
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
    
    @Autowired
    private StockRepository stockRepository;
    
    /**
     * 종목코드를 받아서 현재가, 마지막거래일시를 실시간으로 받아옴(1시간단위캐시)
     * @param code
     * @return
     * @throws IOException 
     */
    @Cacheable(value = "stockCache", key = "#code")
    public RealTimeStock getStockInfo(String code) throws IOException{
        // kospi 일 때만 symbol 규칙이 변경됨
        return getStock(code);
    }
    
    private static synchronized RealTimeStock getStock(String code) throws IOException{
        // kospi 일 때만 symbol 규칙이 변경됨
        Stock yahooStock = "KS11".equals(code) ? YahooFinance.get("^" + code) : YahooFinance.get(code + ".KS");

        return RealTimeStock.builder()
                .currentPrice(yahooStock.getQuote().getPrice())
                .lastTradeTime(sdf.format(yahooStock.getQuote().getLastTradeTime().getTime()))
                .changeInPercent(yahooStock.getQuote().getChangeInPercent())
                .change(yahooStock.getQuote().getChange())
                .yearHigh(yahooStock.getQuote().getYearHigh())
                .changeFromYearHigh(yahooStock.getQuote().getChangeFromYearHigh())
                .changeFromYearHighInPercent(yahooStock.getQuote().getChangeFromYearHighInPercent())
                .currentTime(sdf.format(new Date()))
                .build();
    }

    /**
     * 종목코드 배열을 받아서 현재가 총액 합산하여 반환
     * @param codes
     * @return
     * @throws IOException
     */
    @Cacheable(value = "stockArrayCache")
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
        private BigDecimal change;
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
        com.stocking.modules.stock.Stock stockDB = stockRepository.findByCode(code).orElse(null);
        return getStockHist(code, (stockDB != null) ? stockDB.getCompany() : "");
    }
    
    private static synchronized StockHist getStockHist(String code, String company) {
        
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
            log.error("yahoo finace api 호출 에러",e);
        }
        
        HistoricalQuote maxQuote = quoteList.stream().max(comparatorByClose)
                .orElseThrow(NoSuchElementException::new);
            
        HistoricalQuote minQuote = quoteList.stream().min(comparatorByClose)
            .orElseThrow(NoSuchElementException::new); 
        
        try {
            Thread.sleep(800);
            log.info("0.8초 sleep == " + company);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("sleep 에러" + company, e);
        }
        
        return StockHist.builder()
                .code(code)
                .price(yahooStock.getQuote().getPrice())                     // 현재 시세
                .lastTradeTime(sdf.format(yahooStock.getQuote().getLastTradeTime().getTime()))
                .company(company)
                .change(yahooStock.getQuote().getChange())
                .changeInPercent(yahooStock.getQuote().getChangeInPercent()) // 등락률
                .maxQuote(maxQuote)
                .minQuote(minQuote)
                .quoteList(quoteList.stream().filter(vo -> vo.getClose() != null).collect(Collectors.toList()))
                .build();
        
    }
    
    /**
     * 종목의 장중, 주간, 연간 최고, 최저가 가져오기.
     * @param code
     * @return
     */
    @Cacheable(value = "stockHighLow", key = "#code")
    public StockHighLow getStockHighLow(String code) {
        com.stocking.modules.stock.Stock stockDB = stockRepository.findByCode(code).orElse(null);
        
        return getStockHighLow(code, (stockDB != null) ? stockDB.getCompany() : "");
    }
    
    private static synchronized StockHighLow getStockHighLow(String code, String company) {
        Stock yahooStock = null;
        List<HistoricalQuote> quoteList = null;
        
        Comparator<HistoricalQuote> comparatorByHigh = 
                (x1, x2) -> (x1.getHigh() == null || x2.getHigh() == null) ? 0 :x1.getHigh().compareTo(x2.getHigh());
                
        Comparator<HistoricalQuote> comparatorByLow = 
                (x1, x2) -> (x1.getLow() == null || x2.getLow() == null) ? 0 :x1.getLow().compareTo(x2.getLow());
        
        try {
            yahooStock = "KS11".equals(code) ? YahooFinance.get("^" + code) : YahooFinance.get(code + ".KS");
            Calendar startDt = Calendar.getInstance();
            Calendar endDt = Calendar.getInstance();
            startDt.add(Calendar.DATE, -10);
            quoteList = yahooStock.getHistory(startDt, endDt, Interval.WEEKLY);
        } catch (IOException e) {
            log.error(code + " historical data 가져오는데 실패하였습니다.", e);
        }
        
        HistoricalQuote maxQuote = quoteList.stream().max(comparatorByHigh)
                .orElseThrow(NoSuchElementException::new);
            
        HistoricalQuote minQuote = quoteList.stream().min(comparatorByLow)
            .orElseThrow(NoSuchElementException::new);
        
        StockQuote quote = yahooStock.getQuote();
        
        try {
            Thread.sleep(300);
            log.info("0.3초 sleep == " + company);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("sleep 에러" + company, e);
        }
        
        return StockHighLow.builder()
                .code(code)
                .company(company)
                .price(quote.getPrice())
                .changeInPercent(quote.getChangeInPercent())
                .dayHigh(quote.getDayHigh())
                .dayLow(quote.getDayLow())
                .weekHigh(maxQuote.getHigh())
                .weekLow(minQuote.getLow())
                .yearHigh(quote.getYearHigh())
                .yearLow(quote.getYearLow())
                .build();
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class StockHighLow{
        private String code;
        private String company;
        private BigDecimal price;
        private BigDecimal changeInPercent;
        
        private BigDecimal dayHigh;
        private BigDecimal dayLow;
        private BigDecimal weekHigh;
        private BigDecimal weekLow;
        private BigDecimal yearHigh;
        private BigDecimal yearLow;
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class StockHist{
        private String code;
        private String company;
        private BigDecimal price;
        private String lastTradeTime;
        private BigDecimal change;
        private BigDecimal changeInPercent;
        private HistoricalQuote maxQuote;   // 10년 내 최고가 일자 정보
        private HistoricalQuote minQuote;
        private List<HistoricalQuote> quoteList;
    }
    
    public static String beforeTime(LocalDateTime startDateTime) {
        if(startDateTime == null) return ""; // 기저사례
        return StockUtils.beforeTime(startDateTime, LocalDateTime.now());
    }
    
    /**
     * 일시를 받아서 초전,분전,시전,일전을 계산해서 반환
     * @param localDateTime
     * @return
     */
    public static String beforeTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if(startDateTime == null || endDateTime == null) return ""; // 기저사례

        long diff = ChronoUnit.SECONDS.between(startDateTime, endDateTime);

        long hour = diff / 3600;
        diff = diff % 3600;
        long min = diff / 60;
        long sec = diff % 60;

        String ret;
        if (hour > 24) {
            ret = hour / 24 + "일 전";
        } else if (hour > 0) {
            ret = hour + "시간 전";
        } else if (min > 0) {
            ret = min + "분 전";
        } else if (sec > 0) {
            ret = sec + "초 전";
        } else {
            ret = "지금";
        }
        return ret;
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class StockHighest{
        private HistoricalQuote maxQuote;   // 10년 내 최고가 일자 정보
    }
    
    /**
     * 기간내 최고가 찾기
     * @param code
     * @param investDate
     * @return
     */
    @Cacheable(value = "stockHighestCache")
    public StockHighest getStockHighest(String code, InvestDate investDate) {
        return getStockHighestSync(code, investDate);
    }
    
    public static synchronized StockHighest getStockHighestSync(String code, InvestDate investDate) {
        
        Stock yahooStock = null;
        List<HistoricalQuote> quoteList = null;
        
        Calendar startDt = Calendar.getInstance();
        Calendar endDt = Calendar.getInstance();
        
        switch (investDate) {
            case DAY1 -> startDt.add(Calendar.DATE,-1);
            case WEEK1 -> startDt.add(Calendar.WEEK_OF_YEAR, -1);
            case MONTH1 -> startDt.add(Calendar.MONTH, -1);
            case MONTH6 -> startDt.add(Calendar.MONTH, -6);
            case YEAR1 -> startDt.add(Calendar.YEAR, -1);
            case YEAR5 -> startDt.add(Calendar.YEAR, -5);
            case YEAR10 -> startDt.add(Calendar.YEAR, -10);
            default -> throw new IllegalArgumentException("Unexpected value: " + investDate);
        };
        
        Comparator<HistoricalQuote> comparatorByHigh = 
                (x1, x2) -> (x1.getHigh() == null || x2.getHigh() == null) ? 0 :x1.getHigh().compareTo(x2.getHigh());
        
        try {
            yahooStock = "KS11".equals(code) ? YahooFinance.get("^" + code) : YahooFinance.get(code + ".KS");
            quoteList = yahooStock.getHistory(startDt, endDt, Interval.DAILY);
        } catch (IOException e) {
            log.error("[" + code + "]의 정보를 가져오는데 실패", e);
        }
        
        HistoricalQuote maxQuote = quoteList.stream().max(comparatorByHigh)
                .orElseThrow(NoSuchElementException::new);
        
        try {
            Thread.sleep(300);
            log.info("0.3초 sleep == " + code);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("sleep 에러" + code, e);
        }
        
        return StockHighest.builder()
                .maxQuote(maxQuote)
                .build();
    }
    
    public static LocalDateTime getLocalDateTime(Calendar calendar){
        return  LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }
    
   
}
