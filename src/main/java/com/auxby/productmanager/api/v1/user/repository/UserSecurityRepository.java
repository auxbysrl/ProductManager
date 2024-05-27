package com.auxby.productmanager.api.v1.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSecurityRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByEmail(String username);

}
