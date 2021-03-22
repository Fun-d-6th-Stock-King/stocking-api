package com.stocking.infra.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {
    
    private int pageSize;
    private int pageNo;
    private long count;
}
