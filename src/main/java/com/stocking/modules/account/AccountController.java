package com.stocking.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping(value = "/api/account")
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/login")
    public ResponseEntity<Object> login(Long uuid, Error error) {
        Account account = accountService.findByUuid(uuid);
        if (account == null) {
            return null;
        }
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @GetMapping("/signup")
    public ResponseEntity<Object> signup(@Valid SignUpForm signUpForm, Error error) {
        if (accountService.existByUuid(signUpForm.getUuid())) {
            return null;
        }
        accountService.saveNewAccount(signUpForm);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
