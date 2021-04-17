package com.stocking.modules.todayword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocking.infra.common.FirebaseUser;
import com.stocking.infra.config.UserInterceptor;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
        value = "오늘의 단어 좋아요/안좋아요", 
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
}
