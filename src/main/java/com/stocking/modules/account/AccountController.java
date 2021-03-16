package com.stocking.modules.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping(value = "/api/account")
@RestController
public class AccountController {

    @GetMapping
    public ResponseEntity<Object> login(@Valid SignUpForm signUpForm, Error error) {
        return null;
    }


}
