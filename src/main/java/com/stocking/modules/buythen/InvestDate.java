package com.stocking.modules.buythen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum InvestDate {
    DAY1("1일전"),
    WEEK1("1주전"),
    MONTH1("1개월전"),
    MONTH6("6개월전"),
    YEAR1("1년전"),
    YEAR5("5년전"),
    YEAR10("10년전");
    
    private String name;
    
}
