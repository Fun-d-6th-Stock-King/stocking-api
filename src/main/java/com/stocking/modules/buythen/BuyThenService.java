package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyThenService {

    @Autowired
    private static OldStockRepository oldStockRepository;

    @Autowired
    public static BuyThen getPastStock(BuyThenForm buyThenForm) throws Exception {

        String company = buyThenForm.getCompany();
        System.out.println(company);
        List<OldStock> oldStock1 = oldStockRepository.findAll();
        System.out.println(oldStock1);
        OldStock oldStock = oldStockRepository.findByCompany(company)
                .orElseThrow(() -> new Exception("회사명이 올바르지 않습니다."));;
        String code = oldStock.getCode();
        System.out.println(code);
        String date = buyThenForm.getDate();
        Long price = buyThenForm.getPrice();

        // 과거 주가
        long oldStockPrice = 0;
        if (date == "1년 전") {
            oldStockPrice = oldStock.getOneAgoStock();
        } else if (date == "5년 전") {
            oldStockPrice = oldStock.getFiveAgoStock();
        } else if (date == "10년 전") {
            oldStockPrice = oldStock.getTenAgoStock();
        }

        BuyThen buyThen = new BuyThen(code, company, date, oldStockPrice);
        return buyThen;
    }
}