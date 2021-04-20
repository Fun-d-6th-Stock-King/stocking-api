package com.stocking.modules.account;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    public static final int BEST_STOCKS_SIZE = 9;

    @DisplayName("유저 가입 성공 체크")
    @Transactional
    @Test
    public void saveNewAccountSuccessTest() {

        //given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUuid(Long.valueOf(123123));
        signUpForm.setNickname("TESTER");
        signUpForm.setEmail("test@test.com");
        signUpForm.setCheckSns(false);
        accountService.saveNewAccount(signUpForm);

        //when
        List<Account> accountList = accountRepository.findAll();

        //then
        assertTrue(accountList.stream().anyMatch(account -> account.getUuid().equals(123123)));
    }

    @DisplayName("로그인 화면 주식 목록 조회 기능 체크")
    @Test
    public void getBestStocksTest() {

        //given
        //when
        //then
        assertTrue(accountService.getBestStocks().size() == BEST_STOCKS_SIZE);
    }

}