package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/buythen")
@RestController
public class BuyThenController {

    @Autowired
    private BuyThenService buyThenService;

    @GetMapping
    public ResponseEntity<Object> calculate(
            @ModelAttribute BuyThenForm buyThenForm
    ) throws Exception {
        return new ResponseEntity<>(
                buyThenService.getPastStock(buyThenForm),
                HttpStatus.OK);
    }
}