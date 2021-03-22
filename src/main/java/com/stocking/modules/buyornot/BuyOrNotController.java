package com.stocking.modules.buyornot;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.modules.buyornot.BuyOrNotRes.SimpleEvaluation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

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
        @ApiParam(value = "정렬 조건(최신순 1, 인기순 2)", defaultValue = "1", required = false) @RequestParam(defaultValue = "1") int order,
        @ApiParam(value = "페이지 크기", defaultValue = "10", required = false) @RequestParam(defaultValue = "10") int pageSize,
        @ApiParam(value = "페이지 번호", defaultValue = "1", required = false) @RequestParam(defaultValue = "1") int pageNo
    ) {
        int accountId = 2;
        return new ResponseEntity<>(
            buyOrNotService.getEvaluationList(accountId, stockCode, order, pageSize, pageNo)
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
        @ApiParam(value = "BUY, NOT, NULL", defaultValue = "BUY", required = false) @RequestParam(required = false) BuySell buySell
    ) {
        int accountId = 2;
        
        return new ResponseEntity<>(
            buyOrNotService.saveBuySell(stockCode, accountId, buySell)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "살까말까 상세",
        notes = "살까말까 상세, param - 종목코드, uid, header 에 있는 uid 가져오는 intercepter 만들어야할듯?",
        response = EvaluateBuySellRes.class
    )
    @GetMapping("/{stockCode}")
    public ResponseEntity<Object> getBuyOrNotCount(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode
    ) {
        int accountId = 2;
        
        return new ResponseEntity<>(
            buyOrNotService.getBuySellCount(stockCode, accountId)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "종목별 best 평가 목록",
        notes = "기간(오늘,7일,1개월,6개월,1년,전체기간) type 을 받아서 해당 기간 사이에 좋아요를 가장 많이 받은 순으로 정렬하여 출력",
        response = EvaluateBuySellRes.class
    )
    @GetMapping("/{stockCode}/best")
    public ResponseEntity<Object> getBestList(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode
    ) {
        // 평가 ID
        // 종목명
        // 종목코드
        // 장점
        // 단점
        // 종목 평가의 댓글(최근 1개)
        // 종목 평가의 댓글 총 개수
        // 종목 평가의 댓글단 날짜
        // 좋아요 갯수
        // 사용자의 평가에 좋아요 했는지 여부
        // giphy 이미지 id
        
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }

    @ApiOperation(
        value = "종목별 현재 시세, 등락률, 일별 시세 등",
        notes = "기간(오늘,7일,1개월,6개월,1년,전체기간) type 을 받아서 해당 기간 사이에 좋아요를 가장 많이 받은 순으로 정렬하여 출력",
        response = EvaluateBuySellRes.class
    )
    @PostMapping("/{stockCode}/{beforeDt}/{afterDt}")
    public ResponseEntity<Object> getStockPrice(
        @ApiParam(value = "종목코드", defaultValue = "005930") @PathVariable String stockCode,
        @ApiParam(value = "조회시작일자", defaultValue = "2021-01-01") @PathVariable String beforeDt,
        @ApiParam(value = "조회종료일자", defaultValue = "2021-12-31") @PathVariable String afterDt
    ) throws IOException {
//      * 현재 시세
//      * 등락률
//      * 최근 10년간 최고가
//      * 최고가 일자
//      * 최근 10년간 최저가
//      * 최저가 일자
        
        Stock stock = YahooFinance.get(stockCode, true);
        stock.getQuote().getPrice();            // 현재 시세
        stock.getQuote().getChangeInPercent();  // 등락률
        
        List<HistoricalQuote> list = stock.getHistory();
        
//        list.get(0).get
        
        // 일자별 시세 목록
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
}
