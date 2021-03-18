package com.stocking.modules.account;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

    @Autowired
    AccountRepository accountRepository;

    @DisplayName("유저 가입 체크")
    @Test
    public void saveNewAccount_test() {

        //given
        accountRepository.save(Account.builder()
                .uuid(123123)
                .build());

        //when
        List<Account> accountList = accountRepository.findAll();

        //then
        assertTrue(accountList.stream().anyMatch(account -> account.getUuid().equals(123123)));
    }
}