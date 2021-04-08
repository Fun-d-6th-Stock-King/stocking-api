package com.stocking.infra.common;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

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
        
        Stock yahooStock = YahooFinance.get(code + ".KS");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
        
        return RealTimeStock.builder()
                .currentPrice(yahooStock.getQuote().getPrice())
                .lastTradeTime(sdf.format(yahooStock.getQuote().getLastTradeTime().getTime()))
                .build();
    }
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class RealTimeStock{
        private BigDecimal currentPrice;
        private String lastTradeTime; 
    }
}
