package com.stocking.modules.account;

import com.stocking.modules.buythen.repo.StocksPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final StocksPriceRepository stocksPriceRepository;

    List<String> getBestStocks() {
        List<String> list = null;
        return list;
    }

    public void saveNewAccount(SignUpForm signUpForm) {

        Account account = Account.builder()
                .uuid(signUpForm.getUuid())
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .build();

        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account findByUuid(Long uuid) {
        return accountRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public boolean existByUuid(Long uuid) {
        return accountRepository.existsByUuid(uuid);
    }

}
