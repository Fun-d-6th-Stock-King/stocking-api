package com.stocking.modules.buythen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
    public ResponseEntity<Object> getCalculationHistory(
        @ApiParam(value = "페이지 크기", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo
    ) throws Exception {
        return new ResponseEntity<>(
            buyThenService.getCalculationHistory(pageSize, pageNo)
        , HttpStatus.OK);
    }
    
    @ApiOperation(value = "기간별 수익률로 정렬시킨 목록 조회", 
        notes = "기간별 수익률로 정렬시킨 목록 조회", 
        response = YieldSortRes.class)
    @GetMapping("/yield-list/{buySell}/{investDate}")
    public ResponseEntity<Object> getYieldList(
        @ApiParam(value = "BUY, SELL", defaultValue = "BUY", required = true) @PathVariable BuySell buySell,
        @ApiParam( value = "WEEK1, MONTH1, MONTH6, YEAR1, YEAR5, YEAR10", defaultValue = "WEEK1" , required = true) @PathVariable InvestDate investDate,
        @ApiParam(value = "페이지 크기", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo
    ) throws Exception {
        return new ResponseEntity<>(
            buyThenService.getYieldSortList(investDate, buySell, pageSize, pageNo)
        , HttpStatus.OK);
    }
}