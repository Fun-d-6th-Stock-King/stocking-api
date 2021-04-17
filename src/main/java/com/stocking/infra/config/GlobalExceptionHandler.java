package com.stocking.infra.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.firebase.auth.FirebaseAuthException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    private static final String AUTHORIZATION_MISSING = "Missing request header 'Authorization' for method parameter of type String";
    private static final String FAIL_PARSE_TOKEN = "Failed to parse Firebase ID token";

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    @ResponseBody
    public Map<String, Object> doCommonExceptionProcess(HttpServletResponse httpServletResponse, Exception exception) {
        log.error(getPrintStackTrace(exception));
        
        Map<String, Object> resultMap = new HashMap<>();
        
        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String errorMessage = exception.getLocalizedMessage();

        resultMap.put("message", Optional.ofNullable(errorMessage).orElse("API 서버의 상세로그를 확인하세요."));
        if (exception instanceof ServletRequestBindingException) {
            if(errorMessage.equals(AUTHORIZATION_MISSING)) {
                resultMap.put("solution", "헤더에 firebase 토큰을 추가해주세요. 발급: https://keehyun2.github.io/google-login.html");
            }
        }else if(exception instanceof FirebaseAuthException){
            if(errorMessage.contains(FAIL_PARSE_TOKEN)) {
                resultMap.put("solution", "정확한 token 인지 확인해주세요");
            }
        }
        
        return resultMap;
    }
    
    public static String getPrintStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

}