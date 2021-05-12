package com.stocking.modules.todayword;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.config.UserInterceptor;
import com.stocking.modules.todayword.repo.TodayWord;
import com.stocking.modules.todayword.vo.TodayWordOrder;
import com.stocking.modules.todayword.vo.TodayWordReq;
import com.stocking.modules.todayword.vo.TodayWordRes;
import com.stocking.modules.todayword.vo.TodayWordSortRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = "/api/todayWord")
@RestController
@Api(value = "TodayWordController", tags = "오늘의 단어")
public class TodayWordController {

    @Autowired
    private TodayWordService todayWordService;
    
    @ApiOperation(
        value = "오늘의 단어 등록", 
        response = TodayWord.class
    )
    @PostMapping
    public ResponseEntity<Object> saveTodayWord(
        @ApiIgnore @RequestHeader(UserInterceptor.TOKEN) String token,   // token 필수 처리
        @RequestAttribute(required = false) FirebaseUser user,
        @RequestBody TodayWordReq todayWordReq
    ) {
        return new ResponseEntity<>(
            todayWordService.saveTodayWord(user, todayWordReq)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "오늘의 단어 좋아요/안좋아요", 
        response = TodayWord.class
    )
    @PostMapping("/{id}/like")
    public ResponseEntity<Object> saveTodayWordLike(
        @ApiParam(value = "오늘의 단어 id", required = true) @PathVariable Long id,
        @ApiIgnore @RequestHeader(UserInterceptor.TOKEN) String token,   // token 필수 처리
        @RequestAttribute(required = false) FirebaseUser user
    ) {
        return new ResponseEntity<>(
            todayWordService.saveTodayWordLike(user, id)
        , HttpStatus.OK);
    }
    
    @ApiOperation(
        value = "좋아요가 가장많은 오늘의 단어(메인)", 
        response = TodayWordRes.class
    )
    @GetMapping("/topWord")
    public ResponseEntity<Object> getTopWord(
        @RequestAttribute(required = false) FirebaseUser user
    ) {
        return new ResponseEntity<>(
            todayWordService.getTopWord(user)
        , HttpStatus.OK);
    }

    @ApiOperation(
            value = "오늘의 단어 조회",
            response = TodayWordRes.class
    )
    @GetMapping("/{id}/todayWord")
    public ResponseEntity<Object> getTodayWord(
            @RequestAttribute(required = false) FirebaseUser user,
            @ApiParam(value = "오늘의 단어 id", required = true) @PathVariable Long id) {
        return new ResponseEntity<>(
                todayWordService.getTodayWord(user, id)
            , HttpStatus.OK);
    }

    @ApiOperation(
            value = "오늘의 단어 수정",
            response = Long.class
    )
    @PostMapping("/{id}/update")
    public ResponseEntity<Long> updateTodayWord(
            @ApiIgnore @RequestHeader(UserInterceptor.TOKEN) String token,
            @RequestAttribute(required = false) FirebaseUser user,
            @ApiParam(value = "오늘의 단어 id", required = true) @PathVariable Long id,
            @RequestBody TodayWordReq todayWordReq) {

        return new ResponseEntity<>(
                todayWordService.updateTodayWord(user, todayWordReq, id)
                , HttpStatus.OK);
    }

    @ApiOperation(
            value = "오늘의 단어 목록",
            notes = "오늘의 단어 목록 / param - 정렬순서",
            response = TodayWordSortRes.class
    )
    @GetMapping("/list")
    public ResponseEntity<Object> getTodayWordList(
            @RequestAttribute(required = false) FirebaseUser user,
            @ApiParam(value = "정렬 조건", defaultValue = "LATELY", required = true) @RequestParam TodayWordOrder order,
            @ApiParam(value = "페이지 크기", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNo
    ) {

        return new ResponseEntity<>(
                todayWordService.getTodayWordSortList(user, order, pageSize, pageNo)
                , HttpStatus.OK
        );
    }

}
