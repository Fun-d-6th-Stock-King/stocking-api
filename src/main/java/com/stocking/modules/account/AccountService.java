package com.stocking.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public void saveNewAccount(SignUpForm signUpForm) {

        Account account = Account.builder()
                .uuid(signUpForm.getUuid())
                .nickname(signUpForm.getNickname())
                .build();

        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account findByUuid(Integer uuid) {
        return accountRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public boolean existByUuid(Integer uuid) {
        return accountRepository.existsByUuid(uuid);
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        Account account = accountRepository.findByUserId(userid);
        if (account == null) {
            throw new UsernameNotFoundException(userid);
        }
        account.setPasswd("{noop}" + account.getPasswd());
        return new UserAccount(account);
    }
}
