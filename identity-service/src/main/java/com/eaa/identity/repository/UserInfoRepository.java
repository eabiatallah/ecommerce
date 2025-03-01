package com.eaa.identity.repository;

import com.eaa.identity.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByUsername(String username);
    Optional<UserInfo> findByEmail(String email);

}
