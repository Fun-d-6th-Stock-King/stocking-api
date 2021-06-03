package com.stocking.modules.buyornot.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.stocking.infra.common.StockUtils;
import com.stocking.modules.buythen.repo.StocksPrice;

import io.swagger.annotations.ApiModelProperty;
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
        
        @ApiModelProperty(notes = "장점", position = 1)
        private String pros;
        @ApiModelProperty(notes = "단점", position = 2)
        private String cons;
        @ApiModelProperty(notes = "닉네임", position = 3)
        private String displayName;
        @ApiModelProperty(notes = "작성일시", position = 4)
        private LocalDateTime createdDate;
        @ApiModelProperty(notes = "작성시간-text", position = 5)
        private String createdDateText;
        
        public String getCreatedDateText() {
            return StockUtils.beforeTime(this.createdDate);
        }
    }
    
    @Data
    public static class GroupResult implements Serializable{
        
        private static final long serialVersionUID = 586304960548413137L;
        
        private String code;
        private String company;
        private BigDecimal currentPrice; 
        private BigDecimal change;
        private BigDecimal changeInPercent;
        
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
        
        private int limit;
    }
}
