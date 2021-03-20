package com.stocking.modules.buyornot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/evaluate")
@RestController
public class EvaluateController {
    
    /**
     * 평가 등록
     * param - 종목코드, uid, 장점, 단점, giphy 이미지 id
     */
    @PostMapping
    public ResponseEntity<Object> evaluate() {
        
        // 출력 - evaluate_id
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }

    /**
     * 좋아요
     * param - evaluateId, uid
     */
    @PostMapping("/{evaluateId}/like")
    public ResponseEntity<Object> saveLike() {
        
        // 출력 - evaluate_id
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
    /**
     * 좋아요 취소
     * param - evaluateId, uid
     */
    @DeleteMapping("/{evaluateId}/like")
    public ResponseEntity<Object> saveLikeCancel() {
        
        // 출력 - evaluate_id
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
    
    /**
     * 평가 상세 
     * param - evaluateId, uid
     */
    @GetMapping("/{evaluateId}")
    public ResponseEntity<Object> detail() {
        // 종목 평가 + 평가에 달린 댓글 목록
        // 종목 명
        // 종목 코드
        // 장점
        // 단점
        // 종목 평가의 댓글 목록
        // 종목 평가의 댓글 총 개수
        // 종목 평가의 댓글단 일시
        // 좋아요 개수 
        // 사용자가 평가에 좋아요 했는지 여부
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
     
    /**
     * 평가에 대한 코멘트 등록
     * param - evaluateId, uid, 코멘트 내용
     */
    @GetMapping("/{evaluateId}/comment")
    public ResponseEntity<Object> comment(
            ) {
        // 출력 - commentId
        return new ResponseEntity<>(
            null
        , HttpStatus.OK);
    }
}
