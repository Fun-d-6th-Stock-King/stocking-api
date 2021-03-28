package com.stocking.modules.buyornot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.buyornot.repo.Evaluate;
import com.stocking.modules.buyornot.repo.EvaluateComment;
import com.stocking.modules.buyornot.vo.CommentReq;
import com.stocking.modules.buyornot.vo.EvaluateReq;
import com.stocking.modules.buyornot.vo.EvaluationDetailRes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping(value = "/api/evaluate")
@RestController
@Api(value = "BuyOrNotController", tags = "평가하기")
public class EvaluateController {
    
    @Autowired
    private EvaluateService evaluateService;
    
    @ApiOperation(
        value = "종목별 평가 등록", 
        response = Evaluate.class
    )
    @PostMapping
    public ResponseEntity<Object> evaluate(
        @RequestBody EvaluateReq evaluateReq,
        @RequestAttribute FirebaseUser user
    ) throws Exception {
        return new ResponseEntity<>(
            evaluateService.saveEvaluate(evaluateReq, user.getUid())
        , HttpStatus.OK);
    }

    @ApiOperation(
        value = "좋아요 / 좋아요 취소", 
        notes = "좋아요 한 적이 있으면 데이터를 지우고, 좋아요를 한적이 없으면 좋아요 데이터를 만듬",
        response = Integer.class
    )
    @PostMapping("/{evaluateId}/like")
    public ResponseEntity<Object> saveLike(
        @PathVariable int evaluateId,
        @RequestAttribute FirebaseUser user
    ) {
        return new ResponseEntity<>(
            evaluateService.saveLike(evaluateId, user.getUid())
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "평가에 대한 코멘트 등록", 
        notes = "param - evaluateId, uid, 코멘트 내용",
        response = EvaluateComment.class
    )
    @PostMapping("/{evaluateId}/comment")
    public ResponseEntity<Object> comment(
        @PathVariable int evaluateId,
        @RequestBody CommentReq commentReq,
        @RequestAttribute FirebaseUser user
    ) {
        return new ResponseEntity<>(
            evaluateService.saveComment(evaluateId, commentReq.getComment(), user.getUid())
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "평가 상세", 
        notes = "param - evaluateId, uid",
        response = EvaluationDetailRes.class
    )
    @GetMapping("/{evaluateId}")
    public ResponseEntity<Object> detail(
        @PathVariable int evaluateId,
        @RequestAttribute FirebaseUser user
    ) {
        return new ResponseEntity<>(
            evaluateService.getDetail(evaluateId, user.getUid())
        , HttpStatus.OK);
    }
}
