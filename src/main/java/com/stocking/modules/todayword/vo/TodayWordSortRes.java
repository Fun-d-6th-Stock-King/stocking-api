package com.stocking.modules.todayword.vo;

import com.stocking.infra.common.PageInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayWordSortRes {

    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    @ApiModelProperty(notes = "페이지 정보", required=false, position=1)
    private PageInfo pageInfo;

    @ApiModelProperty(notes = "오늘의 단어 정렬 목록", required=false, position=2)
    private List<TodayWordRes> todayWordResList;

}