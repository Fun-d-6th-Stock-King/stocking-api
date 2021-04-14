package com.stocking.modules.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocking.modules.buythen.repo.QStocksPrice;
import com.stocking.modules.stock.QStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final JPAQueryFactory queryFactory;

    List<String> getBestStocks() {

        QStocksPrice qStocksPrice = QStocksPrice.stocksPrice;
        QStock qStock = QStock.stock;

        List<Long> bestStockIdList = queryFactory.select(qStocksPrice.stocksId)
                .from(qStocksPrice)
                .where(qStocksPrice.marketCap.isNotNull())
                .orderBy(qStocksPrice.marketCap.desc())
                .limit(9)
                .fetch();

        List<String> bestStockNameList = bestStockIdList.stream()
                .map(vo -> {
                    String company = queryFactory.select(qStock.company)
                            .from(qStock)
                            .where(qStock.id.eq(vo))
                            .fetchOne();
                    return company;
                }).collect(Collectors.toList());

        return bestStockNameList;
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
