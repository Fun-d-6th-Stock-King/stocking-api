package com.stocking.modules.todayword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ) throws Exception {
        return new ResponseEntity<>(
            todayWordService.saveTodayWord(user, todayWordReq)
        , HttpStatus.OK);
    }
}
