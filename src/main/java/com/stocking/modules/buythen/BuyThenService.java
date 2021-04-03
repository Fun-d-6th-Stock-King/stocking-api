package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuyThenService {

    @Autowired
    private static OldStockRepository oldStockRepository;

    @Autowired
    public static BuyThen getPastStock(BuyThenForm buyThenForm) {

        String company = buyThenForm.getCompany();
        OldStock oldStock = oldStockRepository.findByCompany(company);
        String code = oldStock.getCode();
        String date = buyThenForm.getDate();
        Long price = buyThenForm.getPrice();

        BuyThen buyThen = new BuyThen(code, company, date, price);
        return buyThen;
    }
}