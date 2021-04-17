package com.stocking.infra.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Component
public class StockUtils {

    /**
     * 종목코드를 받아서 현재가, 마지막거래일시를 실시간으로 받아옴(1시간단위캐시)
     * @param code
     * @return
     * @throws IOException 
     */
    @Cacheable(value = "stockCache", key = "#code")
    public RealTimeStock getStockInfo(String code) throws IOException{
        Stock yahooStock;
        if (code == "KS11") { // kospi 일 때만 symbol 규칙이 변경됨
            yahooStock = YahooFinance.get("^" + code);
        } else {
            yahooStock = YahooFinance.get(code + ".KS");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
        Date now = new Date();

        return RealTimeStock.builder()
                .currentPrice(yahooStock.getQuote().getPrice())
                .lastTradeTime(sdf.format(yahooStock.getQuote().getLastTradeTime().getTime()))
                .currentTime(sdf.format(now))
                .build();
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class RealTimeStock{
        private BigDecimal currentPrice; // 현재 주가
        private String lastTradeTime;    // 최근 거래 일시
        private String currentTime;      // 현재가를 업데이트한 시간
    }
}
