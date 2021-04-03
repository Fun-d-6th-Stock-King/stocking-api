package com.stocking.modules.buythen;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuyThen {

    private String code;    // 종목 코드
    private String company; // 회사명
    private String date;    // 날짜 ex) 2020-12-31
    private Long price;     // 그때의 주가
}