package com.example.demo.user.service.port;

import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserStatus;

import java.util.Optional;

public interface UserRepository {

    User save(User userEntity);

    Optional<User> findById(long id);

    Optional<User> findByIdAndStatus(long id, UserStatus userStatus);

    Optional<User> findByEmailAndStatus(String email, UserStatus userStatus);
}