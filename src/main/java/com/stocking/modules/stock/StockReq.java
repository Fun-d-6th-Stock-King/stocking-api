package com.stocking.modules.stock;

import com.stocking.infra.common.PageParam;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StockReq extends PageParam {

    @ApiParam(value = "정렬 방향(asc, desc)", defaultValue = "desc", required = false)
    private String sortDirection;

    @ApiParam(value = "정렬 필드명", defaultValue = "id", required = false)
    private String sortColumn = "id"; // 정렬할 필드 명
}
