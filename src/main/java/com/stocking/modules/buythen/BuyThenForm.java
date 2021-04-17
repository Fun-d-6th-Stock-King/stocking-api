package com.stocking.modules.buythen;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyThenForm {

    @NotBlank
    @ApiModelProperty(notes = "투자 시기", example = "YEAR1", required = true)
    private InvestDate investDate;

    @NotBlank
    @ApiModelProperty(notes = "종목코드", example = "005930")
    private String code;

    @NotBlank
    @ApiModelProperty(notes = "투자금", example = "100000")
    private BigDecimal investPrice;
    
}