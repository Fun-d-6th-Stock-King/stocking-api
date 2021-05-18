package com.stocking.modules.firebase;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FireUserRepository extends JpaRepository<FireUser, Long> {

    public Optional<FireUser> findByUid(String uid);
}