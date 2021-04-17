package com.stocking.modules.buyornot;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.buyornot.repo.EvaluateBuySell.BuySell;
import com.stocking.modules.buyornot.vo.BuyOrNotOrder;
import com.stocking.modules.buyornot.vo.BuyOrNotPeriod;
import com.stocking.modules.buyornot.vo.BuyOrNotRes.SimpleEvaluation;
import com.stocking.modules.buyornot.vo.BuySellRankRes;
import com.stocking.modules.buyornot.vo.BuySellRankRes.RankListType;
import com.stocking.modules.buyornot.vo.EvaluateBuySellRes;
import com.stocking.modules.buyornot.vo.EvaluationRes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import yahoofinance.histquotes.Interval;

@RequestMapping(value = "/api/buyornot")
@RestController
@Api(value = "BuyOrNotController", tags = "살까말까")
public class BuyOrNotController {

    @Autowired
    private BuyOrNotService buyOrNotService;
    
    @ApiOperation(
        value = "전체 평가 목록",
        notes = "살까 말까 메인 장단점 평가 목록 / param - 검색어, 정렬순서",
        response = EvaluationRes.class
    )
    @GetMapping
    public ResponseEntity<Object> getBuyOrNotList(
        @ApiParam(value = "정렬 조건", defaultValue = "LATELY") @RequestParam(defaultValue = "LATELY") BuyOrNotOrder order,
        @ApiParam(value = "페이지 크기", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo,
        @ApiParam(value = "검색어", required = false) @RequestParam(required = false) String searchWord
    ) {
        
        return new ResponseEntity<>(
            buyOrNotService.getBuyOrNotList(order, pageSize, pageNo, searchWord)
        , HttpStatus.OK);
    }

    @ApiOperation(
        value = "종목별 전체 평가 목록", 
        notes = "종목별 전체 평가 목록 param - 종목코드, 정렬조건(최신순 1, 인기순 2), uid",
        response = EvaluationRes.class
    )
    @GetMapping("/{stockCode}/evaluate")
    public ResponseEntity<Object> getEvaluationList(
        @ApiParam(value = "종목코드", defaultValue = "005930", required = true ) @PathVariable String stockCode,
        @ApiParam(value = "정렬 조건", defaultValue = "LATELY" ) @RequestParam(defaultValue = "LATELY") BuyOrNotOrder order,
        @ApiParam(value = "페이지 크기", defaultValue = "10", required = false) @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1", required = false) @RequestParam(defaultValue = "1") int pageNo,
        @RequestAttribute(required = true) FirebaseUser user
    ) {
        return new ResponseEntity<>(
            buyOrNotService.getEvaluationList(user.getUid(), stockCode, order, pageSize, pageNo)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "오늘의 베스트",
        notes = "오늘 좋아요를 가장 많이 받은 평가",
        response = SimpleEvaluation.class
    )
    @GetMapping("/today-best")
    public ResponseEntity<Object> todayBest() {
        
        return new ResponseEntity<>(
            buyOrNotService.getTodayBest()
        , HttpStatus.OK);
    }

    @ApiOperation(
        value = "종목 살래말래 평가 하기",
        notes = "종목 살래말래 평가 하기",
        response = Integer.class
    )
    @PostMapping("/{stockCode}")
    public ResponseEntity<Object> buyOrNot(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode,
        @ApiParam(value = "BUY, NOT, NULL", defaultValue = "BUY", required = false) @RequestParam(required = false) BuySell buySell,
        @RequestAttribute FirebaseUser user
    ) {
        return new ResponseEntity<>(
            buyOrNotService.saveBuySell(stockCode, user.getUid(), buySell)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "살까말까 상세",
        notes = "살까말까 상세, param - 종목코드, uid, header 에 있는 uid 가져오는 intercepter 만들어야할듯?",
        response = EvaluateBuySellRes.class
    )
    @GetMapping("/{stockCode}")
    public ResponseEntity<Object> getBuyOrNotCount(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode,
        @RequestAttribute FirebaseUser user
    ) {
        return new ResponseEntity<>(
            buyOrNotService.getBuySellCount(stockCode, user.getUid())
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "종목별 best 평가 목록",
        notes = "기간(오늘,7일,1개월,6개월,1년) type 을 받아서 해당 기간 사이에 좋아요를 가장 많이 받은 순으로 정렬하여 출력",
        response = EvaluationRes.class
    )
    @GetMapping("/{stockCode}/best")
    public ResponseEntity<Object> getBestList(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode,
        @ApiParam(value = "기간", defaultValue = "TODAY" ) @RequestParam(defaultValue = "TODAY") BuyOrNotPeriod period,
        @ApiParam(value = "페이지 크기", defaultValue = "10", required = false) @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1", required = false) @RequestParam(defaultValue = "1") int pageNo,
        @RequestAttribute(required = false) FirebaseUser user
    ) {
        
        return new ResponseEntity<>(
            buyOrNotService.getBestEvaluationList(user.getUid(), stockCode, period, pageSize, pageNo)
        , HttpStatus.OK);
    }

    @ApiOperation(
        value = "종목별 현재 시세, 등락률, 일별 시세 등",
        notes = "현재 시세, 등락률, 기간내 최고가, 최고가 일자, 기간내 최저가, 최저가 일자",
        response = EvaluateBuySellRes.class
    )
    @GetMapping("/{stockCode}/{beforeDt}/{afterDt}")
    public ResponseEntity<Object> getStockPrice(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode,
        @ApiParam(value = "조회시작일자", defaultValue = "2021-01-01") @PathVariable String beforeDt,
        @ApiParam(value = "조회종료일자", defaultValue = "2021-12-31") @PathVariable String afterDt,
        @ApiParam(value = "조회 간격", defaultValue = "DAILY") @RequestParam(defaultValue = "DAILY") Interval interval
    ) throws IOException, ParseException {
        
        return new ResponseEntity<>(
                buyOrNotService.getStockPrice(stockCode, beforeDt, afterDt, interval)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "살까 말까 랭킹 목록(메인)",
        notes = "살까 말까 랭킹 목록(메인)",
        response = BuySellRankRes.class
    )
    @GetMapping("/getBuyRankList/{buySell}/{rankListType}")
    public ResponseEntity<Object> getBuyRankList(
        @ApiParam(value = "BUY, SELL", defaultValue = "BUY") @PathVariable BuySell buySell,
        @ApiParam(value = "리스트타입", defaultValue = "SIMPLE") @PathVariable RankListType rankListType
    ) {
        
        return new ResponseEntity<>(
            buyOrNotService.getBuyRankList(buySell, rankListType)
        , HttpStatus.OK);
    }
    
}
