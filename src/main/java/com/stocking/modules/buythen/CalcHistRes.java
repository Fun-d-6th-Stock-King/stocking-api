package com.stocking.modules.buythen;

import java.math.BigDecimal;
import java.util.List;

import com.stocking.infra.common.PageInfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CalcHistRes {
	
	private List<CalculationHist> calculationHistList;
	
	private PageInfo pageInfo;
 
	@Data
	@AllArgsConstructor
	@Builder
    public static class CalculationHist {
    	@ApiModelProperty(notes = "id", required=false, position=1)
        private long id;

    	@ApiModelProperty(notes = "종목코드", required=false, position=2)
        private String code;

    	@ApiModelProperty(notes = "회사명", required=false, position=3)
        private String company;
        
    	@ApiModelProperty(notes = "투자시기", required=false, position=4)
        private String investDate;
    	
    	@ApiModelProperty(notes = "투자시기 한글명", required=false, position=4)
        private String investDateName;
        
    	@ApiModelProperty(notes = "투자금", required=false, position=5)
        private BigDecimal investPrice;
        
    	@ApiModelProperty(notes = "수익금", required=false, position=6)
        private BigDecimal yieldPrice;
        
    	@ApiModelProperty(notes = "수익률", required=false, position=7)
        private BigDecimal yieldPercent;
        
    	@ApiModelProperty(notes = "현재가(검색시점)", required=false, position=8)
        private BigDecimal price;
        
        @ApiModelProperty(notes = "검색일시", required=false, position=9)
        private String createdDate;
        
        @ApiModelProperty(notes = "작성자 id", required=false, position=10)
        private String createdUid;
        
        
        @ApiModelProperty(notes = "종목코드", required=false, position=12)
        private String sector;
        
        @ApiModelProperty(notes = "종목코드", required=false, position=13)
        private String sectorKor;
    }
    
}
