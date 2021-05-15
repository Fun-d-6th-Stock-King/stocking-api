package com.stocking.infra.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.stocking.infra.common.FirebaseUser;
import com.stocking.modules.firebase.FireUserService;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
	
	public static final String TOKEN = "Authorization";
	public static final String USER = "user";
	
	@Autowired
	private FireUserService fireUserService;
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws NotFoundException, FirebaseAuthException {
        String token = request.getHeader(TOKEN);
        if(!StringUtils.isEmpty(token)) {
        	log.info("token : " + token);
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            request.setAttribute(USER, FirebaseUser.builder()
                    .email(decodedToken.getEmail())
                    .name(decodedToken.getName())
                    .uid(decodedToken.getUid())
                    .build());
            
            fireUserService.save(decodedToken); // 사용자 저장
        }else {
        	request.setAttribute(USER, FirebaseUser.builder().build());
        }
        
        return true;
    }
    
}