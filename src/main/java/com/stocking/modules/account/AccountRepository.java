package com.stocking.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account save(Account account);

    boolean existsByUuid(Long uuid);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<Account> findById(Long id);

    Optional<Account> findByEmail(String email);

    Account findByUuid(Long uuid);
}
