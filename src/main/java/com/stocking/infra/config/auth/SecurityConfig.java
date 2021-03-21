package com.stocking.infra.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 접근 권한 모든 접근자에게 할당
        http.authorizeRequests()
                .mvcMatchers("/", "/api/**", "/v2/api-docs",
                        "/configuration/ui", "/configuration/security", "/swagger-resources",
                         "/swagger-ui.html", "/webjars/**", "/swagger/**").permitAll();
//                anyRequest().authenticated();

        // security 에서 post 요청은 보안을 위해 csrf 토큰을 사용함. 나중에 설정하고 일단 비활성화 시킴
        http.csrf().disable();

        http.oauth2Login()
                .userInfoEndpoint()
                    .userService(customOAuth2UserService);
    }

}
