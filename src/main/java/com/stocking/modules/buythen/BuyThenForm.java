package com.stocking.modules.buythen;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class BuyThenForm {

    @NotBlank
    @ApiModelProperty(notes = "투자 시기", example = "1년 전")
    private String date;

    @NotBlank
    @ApiModelProperty(notes = "회사명", example = "삼성전자")
    private String company;

    @NotBlank
    @ApiModelProperty(notes = "투자금", example = "100000")
    private Long price;
}