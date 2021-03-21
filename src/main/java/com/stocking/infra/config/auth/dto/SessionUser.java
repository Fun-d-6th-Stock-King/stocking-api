package com.stocking.infra.config.auth.dto;

import com.stocking.modules.account.Account;

import java.io.Serializable;

public class SessionUser implements Serializable {
    private Long id;
    private String email;

    public SessionUser(Account account) {
        this.id = account.getId();
        this.email = account.getEmail();
    }
}
