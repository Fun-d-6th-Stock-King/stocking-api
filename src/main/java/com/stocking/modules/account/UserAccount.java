package com.stocking.modules.account;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {
        super(account.getUserId(), account.getPasswd(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
