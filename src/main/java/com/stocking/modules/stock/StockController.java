package com.stocking.modules.stock;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/stock")
@RestController
public class StockController {

  @Autowired
  private StockService financeService;

  @GetMapping
  public ResponseEntity<Object> getStockList(@Valid StockReq financeReq) {
    return new ResponseEntity<>(financeService.getStockList(financeReq), HttpStatus.OK);
  }
}
