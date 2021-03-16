package com.stocking.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        Account account = accountRepository.findByUuid(uuid);
        if (account == null) {
            throw new UsernameNotFoundException(uuid);
        }
        account.setPasswd("{noop}" + account.getPasswd());
        return new UserAccount(account);
    }
}
