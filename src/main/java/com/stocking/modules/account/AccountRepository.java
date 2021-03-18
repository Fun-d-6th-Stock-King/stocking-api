package com.stocking.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Account save(Account account);

    boolean existsByUuid(Integer uuid);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByUuid(Integer uuid);

    Account findByUserId(String userid);
}
