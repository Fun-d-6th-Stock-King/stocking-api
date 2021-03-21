package com.stocking.modules.buyornot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/evaluate")
@RestController
public class EvaluateController {
    
    @Autowired
    private EvaluateService evaluateService;
    
    /**
     * 평가 등록
     * param - 종목코드, uid, 장점, 단점, giphy 이미지 id
     * @throws Exception 
     */
    @PostMapping
    public ResponseEntity<Object> evaluate(
        @RequestBody EvaluateReq evaluateReq
    ) throws Exception {
        int accountId = 2;
        
        return new ResponseEntity<>(
            evaluateService.saveEvaluate(evaluateReq, accountId)
        , HttpStatus.OK);
    }

    /**
     * 좋아요 / 좋아요 취소
     * 좋아요 한 적이 있으면 데이터를 지우고, 좋아요를 한적이 없으면 좋아요 데이터를 만듬
     * param - evaluateId, uid
     * @throws Exception 
     */
    @PostMapping("/{evaluateId}/like")
    public ResponseEntity<Object> saveLike(
        @PathVariable int evaluateId
    ) {
        int accountId = 2;
        
        return new ResponseEntity<>(
            evaluateService.saveLike(evaluateId, accountId)
        , HttpStatus.OK);
    }
    
    /**
     * 평가에 대한 코멘트 등록
     * param - evaluateId, uid, 코멘트 내용
     */
    @PostMapping("/{evaluateId}/comment")
    public ResponseEntity<Object> comment(
        @PathVariable int evaluateId,
        @RequestBody CommentReq commentReq
    ) {
        int accountId = 2;
        
        return new ResponseEntity<>(
            evaluateService.saveComment(evaluateId, commentReq.getComment(), accountId)
        , HttpStatus.OK);
    }
    
    /**
     * 평가 상세 
     * param - evaluateId, uid
     */
    @GetMapping("/{evaluateId}")
    public ResponseEntity<Object> detail() {

        // 종목 명
        // 종목 코드
        // 장점
        // 단점
        // 종목 평가의 댓글 목록
        // 종목 평가의 댓글 총 개수
        // 종목 평가의 댓글단 일시
        // 좋아요 개수 
        // 사용자가 평가에 좋아요 했는지 여부
        
        // 평가에 달린 댓글 목록
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
}
