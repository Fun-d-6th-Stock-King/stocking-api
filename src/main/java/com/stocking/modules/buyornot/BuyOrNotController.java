package com.stocking.modules.buyornot;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RequestMapping(value = "/api/buyornot")
@RestController
@Api(value = "BuyOrNotController", tags = "buyornot")
public class BuyOrNotController {

    @Autowired
    private BuyOrNotService buyOrNotService;

    /**
     * 전체 평가 목록 param - 종목코드, 정렬조건(최신순 1, 인기순 2), uid
     */
    @ApiOperation(
        value = "전체 평가 목록", 
        response = EvaluationRes.class, 
        tags = "buyornot"
    )
    @GetMapping("/{stockCode}/evaluate")
    public ResponseEntity<Object> getEvaluationList(
        HttpServletRequest request, 
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
    
    /**
     * 오늘의 베스트
     * 아니면 오늘 좋아요를 가장 많이 받은 평가
     */
    @GetMapping("/today-best")
    public ResponseEntity<Object> todayBest() {
        
        // 좋아요를 가장 많이 받은 평가의 id 조회 
        // 평가 id 로 평가 단건 조회
        
        // 출력 - 종목명, 종목코드, 장점, 단점, 등록자 uid
        return new ResponseEntity<>(
            buyOrNotService.getTodayBest()
        , HttpStatus.OK);
    }

    /**
     * 살까 말까 메인 장단점 평가 목록
     * param - 검색어, 정렬순서
     */
    @GetMapping
    public ResponseEntity<Object> getBuyOrNotList(String searchWord,
            String sort) {
        
        // 출력 - 종목명, 종목코드, 장점, 단점, 등록자 uid
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
    /**
     * 종목 살래말래 평가 하기
     * param 
     * - stockCode 
     * - uid
     * - 살래, 말래 여부 (1-살래, 2-말래, 3-미선택)
     */
    @PostMapping("/{stockCode}")
    public ResponseEntity<Object> buyOrNot() {
        // stockCode
        
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
    /**
     * 살까말까 상세(종목 상세)
     * uid 일단 하드코딩으로 넣을 예정, 
     * header 에 있는 uid 가져오는 intercepter 만들어야할듯? 
     * param - 종목코드, uid
     */
    @GetMapping("/{stockCode}")
    public ResponseEntity<Object> getStockDetail() {
        
//        * 현재 시세
//        * 등락률
//        * 최근 10년간 최고가
//        * 최고가 일자
//        * 최근 10년간 최저가
//        * 최저가 일자
//        * 살래 갯수
//        * 말래 갯수
    	
//        * 사용자의 선택여부(1-살래, 2-말래, 3-미선택)
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
    /**
     * best 평가 목록
     * param - 기간, uid
     */
    @GetMapping("/{stockCode}/best")
    public ResponseEntity<Object> getBestList() {
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

    /**
     * 종목별 일별 시세 
     */
    @PostMapping("/{stockCode}/{beforeDt}/{afterDt}")
    public ResponseEntity<Object> getDailyPrice(
            ) {
        // 일자별 시세 목록
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
}
