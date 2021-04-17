package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.common.PageParam;
import com.stocking.modules.buythen.repo.CalcHist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping(value = "/api/buythen")
@RestController
@Api(value = "BuyThenController", tags = "그때 살껄")
public class BuyThenController {

    @Autowired
    private BuyThenService buyThenService;
    
    @ApiOperation(value = "kospi 종목 목록 불러오기", notes = "종목 목록 불러오기", response = StockRes.class)
    @GetMapping
    public ResponseEntity<Object> getStockList() throws Exception {
        return new ResponseEntity<>(buyThenService.getStockList(), HttpStatus.OK);
    }

    @ApiOperation(value = "종목 수익률 계산기", notes = "메인 계산기", response = CalculatedRes.class)
    @GetMapping("/calculate")
    public ResponseEntity<Object> calculate(
		@ModelAttribute BuyThenForm buyThenForm,
		@RequestAttribute(required = false) FirebaseUser user
//		@ApiIgnore @RequestHeader(UserInterceptor.TOKEN) String token	// token 필수 처리
	) throws Exception {
        return new ResponseEntity<>(buyThenService.getPastStock(buyThenForm, user), HttpStatus.OK);
    }

    @ApiOperation(value = "현재가, 코스피, 동일업 정보 불러오기", notes = "현재가, 코스피, 동일업종", response = CurrentKospiIndustryRes.class)
    @GetMapping("/current-kospi-industry")
    public ResponseEntity<Object> getCurrentKospiIndustry(@ModelAttribute BuyThenForm buyThenForm) throws Exception {
        return new ResponseEntity<>(buyThenService.getCurrentKospiIndustry(buyThenForm), HttpStatus.OK);
    }
    
    @ApiOperation(value = "수익금 계산 이력 목록 조회", notes = "수익금 계산 이력 목록 조회 - 페이징처리된", response = CalcHistRes.class)
    @GetMapping("/calculation-history")
    public ResponseEntity<Object> getCalculationHistory(@ModelAttribute PageParam pageParam) throws Exception {
        return new ResponseEntity<>(buyThenService.getCalculationHistory(pageParam), HttpStatus.OK);
    }
}