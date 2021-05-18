package com.stocking.modules.buyornot.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class CompanyRes {
    
    private List<Company> companyList;

    @Data
    public static class Company {
        
        @ApiModelProperty(notes = "종목코드", position = 1)
        private String code;
        
        @ApiModelProperty(notes = "회사명", position = 2)
        private String name;
    }
}
