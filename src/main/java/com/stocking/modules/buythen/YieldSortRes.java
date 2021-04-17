package com.stocking.modules.buythen;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.stocking.infra.common.PageInfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Builder
public class YieldSortRes {
    
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; 
	
    @ApiModelProperty(notes = "수익률 정렬 목록", required=false, position=1)
	private List<YieldSort> yieldSortList;
	
	@ApiModelProperty(notes = "기준일자", required=false, position=1)
    private LocalDateTime updatedDate;
	
	public String getUpdatedDate() {
	    return updatedDate.format(DateTimeFormatter.ofPattern(FORMAT));
	}
	
	@ApiModelProperty(notes = "페이지 정보", required=false, position=1)
	private PageInfo pageInfo;
 
	@Getter
	@AllArgsConstructor
	@Builder
    public static class YieldSort {
	    
    	@ApiModelProperty(notes = "id", required=false, position=1)
        private long id;

    	@ApiModelProperty(notes = "종목코드", required=false, position=2)
        private String code;

    	@ApiModelProperty(notes = "회사명", required=false, position=3)
        private String company;
    	
    	@ApiModelProperty(notes = "종목 분류", required=false, position=4)
        private String sector;
        
        @ApiModelProperty(notes = "종목 분류 한글명", required=false, position=5)
        private String sectorKor;
        
    	@ApiModelProperty(notes = "그때 가격", required=false, position=6)
        private BigDecimal oldPrice;
    	
    	@ApiModelProperty(notes = "그때 일자", required=false, position=7)
        private LocalDateTime oldDate;
        
    	@ApiModelProperty(notes = "수익률", required=false, position=8)
        private BigDecimal yieldPercent;
    	
    	@ApiModelProperty(notes = "기준일자", required=false, position=9)
        private LocalDateTime updatedDate;
        
    	@ApiModelProperty(notes = "현재가(기준일자 기준)", required=false, position=10)
        private BigDecimal price;
        
    }
    
}
