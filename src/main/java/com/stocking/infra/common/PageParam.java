package com.stocking.infra.common;

import javax.validation.constraints.Min;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public abstract class PageParam {

    @ApiParam(value = "페이지 번호", defaultValue = "0", required = true)
    @Min(value = 0, message = "페이지 번호는  0보다 커야합니다.")
    private int page;

    @ApiParam(value = "페이지 크기", defaultValue = "10", required = true)
    @Min(value = 10, message = "페이지 사이즈는 10보다 커야합니다.")
    private int size;

}
