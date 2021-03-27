package com.stocking.infra.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.stocking.modules.firebase.FireUserRes;
import com.stocking.modules.firebase.FireUserService;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInterceptor implements HandlerInterceptor {
	
	public static final String UID = "Uid";
	public static final String USER = "user";
	
	private final FireUserService fireUserService;
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws NotFoundException {
        String uid = request.getHeader(UID);
        if(!StringUtils.isEmpty(uid)) {
            request.setAttribute(USER, fireUserService.getFireUser(uid.trim()));
        } else {
            request.setAttribute(USER, FireUserRes.builder().build());
        }
        return true;
    }
    
}