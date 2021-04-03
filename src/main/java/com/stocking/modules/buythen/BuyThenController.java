package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping(value = "/api/buythen")
@RestController
@Api(value = "BuyThenController", tags = "그때 살껄")
public class BuyThenController {

    @Autowired
    private BuyThenService buyThenService;
    
    @ApiOperation(
        value = "종목 수익률 계산기",
        notes = "메인 계산기",
        response = BuyThen.class
    )
    @GetMapping
    public ResponseEntity<Object> calculate(
        @ModelAttribute BuyThenForm buyThenForm
    ) throws Exception {
        return new ResponseEntity<>(
                buyThenService.getPastStock(buyThenForm),
                HttpStatus.OK);
    }
}