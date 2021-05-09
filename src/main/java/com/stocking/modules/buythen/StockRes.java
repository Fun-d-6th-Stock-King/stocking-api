package com.stocking.modules.buythen;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class StockRes {
    
    private long count;
    
    private List<Company> companyList;

    @AllArgsConstructor
    @Builder
    @Data
    public static class Company {
        
        @ApiModelProperty(notes = "회사명", position = 1)
        private String company;

        @ApiModelProperty(notes = "종목코드", position = 2)
        private String code;
        
        @ApiModelProperty(notes = "조회 가능한 날짜", position = 3)
        private List<InvestDate> vaildDateList;
    }
}
