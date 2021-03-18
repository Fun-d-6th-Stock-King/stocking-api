package com.stocking.modules.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    /**
     * 상장기업 목록 조회
     * 
     * @param financeReq
     * @return
     */
    public Page<Stock> getStockList(StockReq financeReq) {
        Sort sort = Sort.by(Direction.fromOptionalString(financeReq.getSortDirection()).orElse(Direction.ASC),
                financeReq.getSortColumn());
        return stockRepository.findAll(PageRequest.of(financeReq.getPage(), financeReq.getSize(), sort));
    }
}
