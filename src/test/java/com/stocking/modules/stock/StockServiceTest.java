package com.stocking.modules.stock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StockServiceTest {

    @Autowired
    private StockService financeService;

    public static final Long DEFAULT_STOCKS_PAGE_NUMBER = 0L;
    public static final Long DEFAULT_STOCKS_PAGE_SIZE = 10L;

    @Test
    public void getStockListSuccessTest() {

        //given
        StockReq financeReq = new StockReq();
        financeReq.setPage(DEFAULT_STOCKS_PAGE_NUMBER);
        financeReq.setSize(DEFAULT_STOCKS_PAGE_SIZE);

        //when
        //then
        assertTrue(financeService.getStockList(financeReq).getSize() == DEFAULT_STOCKS_PAGE_SIZE);
    }

    @Test
    public void getStockListFailTest() {

        //given
        StockReq financeReq = new StockReq();
        financeReq.setPage(DEFAULT_STOCKS_PAGE_NUMBER);
        financeReq.setSize(DEFAULT_STOCKS_PAGE_SIZE - 1L);

        //when
        //then
        assertFalse(financeService.getStockList(financeReq).getSize() == DEFAULT_STOCKS_PAGE_SIZE);
    }
}
