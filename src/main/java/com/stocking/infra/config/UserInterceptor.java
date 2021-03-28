package com.stocking.infra.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.stocking.infra.common.FirebaseUser;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
	
	public static final String TOKEN = "Authorization";
	public static final String USER = "user";
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws NotFoundException, FirebaseAuthException {
        
        String token = request.getHeader(TOKEN);
        log.info("token : " + token);
        if(!StringUtils.isEmpty(token)) {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            request.setAttribute(USER, FirebaseUser.builder()
                    .email(decodedToken.getEmail())
                    .name(decodedToken.getName())
                    .uid(decodedToken.getUid())
                    .build());
        }
        
        return true;
    }
    
}