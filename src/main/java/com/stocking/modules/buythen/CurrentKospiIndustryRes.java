package com.stocking.modules.buythen;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class CurrentKospiIndustryRes {

    // 공통
    @ApiModelProperty(notes = "종목코드", position = 1)
    private String code;
    @ApiModelProperty(notes = "회사명", position = 2)
    private String company;

    @ApiModelProperty(notes = "코스피 섹션 결과", position = 3)
    private KospiValue kospiValue;
    @ApiModelProperty(notes = "동종업종 섹션 결과", position = 4)
    private IndustryValue industryValue;
    @ApiModelProperty(notes = "현재가 섹션 결과", position = 5)
    private CurrentValue currentValue;

    // 코스피 섹션 결과값
    @Data
    @AllArgsConstructor
    @Builder
    public static class KospiValue {
        @ApiModelProperty(notes = "코스피 상승률", position = 6)
        private BigDecimal yieldPercent;
        @ApiModelProperty(notes = "코스피 계산 기준 시점 날짜", position = 7)
        private String oldDate;
        @ApiModelProperty(notes = "계산 기준 시점의 코스피 지수 ", position = 8)
        private BigDecimal oldStock;
        @ApiModelProperty(notes = "현재 코스피 지수", position = 9)
        private BigDecimal currentStock;
        @ApiModelProperty(notes = "현재 시각", position = 10)
        private String currentTime;
    }

    // 동종 업종 섹션 결과값
    @Data
    @AllArgsConstructor
    @Builder
    public static class IndustryValue {
        @ApiModelProperty(notes = "업종명", position = 11)
        private String industryName;
        @ApiModelProperty(notes = "동일업종 상승률", position = 12)
        private String industryYieldPercent;
        @ApiModelProperty(notes = "동일업종 기업", position = 13)
        private String industryCompanies;
        @ApiModelProperty(notes = "동일업종 기업 수(-4 계산값)", position = 14)
        private String industryCnt;
    }

    // 믿고싶지 않은 현재가 섹션 결과값
    public static class CurrentValue {
        @ApiModelProperty(notes = "1주당 가격", position = 15)
        private BigDecimal pricePerStock;
        @ApiModelProperty(notes = "보유 종목수 환산 정보", position = 16)
        private String stockPerPrice;
        @ApiModelProperty(notes = "현재 시각", position=17)
        private String currentTime;
    }
}
