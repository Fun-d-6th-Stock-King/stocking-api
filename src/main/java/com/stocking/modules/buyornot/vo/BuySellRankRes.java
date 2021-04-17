package com.stocking.modules.buyornot.vo;

import java.io.Serializable;
import java.util.List;

import com.stocking.modules.buythen.repo.StocksPrice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class BuySellRankRes {
    
    private RankTop rankTop;
    private List<GroupResult> groupResultList;
    
    @Builder
    @AllArgsConstructor
    @Getter
    public static class RankTop implements Serializable {
        private static final long serialVersionUID = -8114380780422205158L;
        
        private String pros;
        private String cons;
    }
    
    @Data
    public static class GroupResult implements Serializable{
        
        private static final long serialVersionUID = 586304960548413137L;
        
        private String code;
        private String company;
        private String sectorYahoo;
        private String sectorYahooKor;
        private Long buyCnt;         
        private Long sellCnt;
        
        public String getSectorYahooKor() {
            this.sectorYahooKor = StocksPrice.SECTOR_YAHOO.get(sectorYahoo);
            return this.sectorYahooKor;
        }
    }
    
    @AllArgsConstructor
    @Getter
    public enum RankListType{
        SIMPLE(3), DETAIL(10);
        
        private int max;
    }
}
