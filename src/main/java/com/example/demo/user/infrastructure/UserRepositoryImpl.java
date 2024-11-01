package com.example.demo.user.infrastructure;

import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    final UserJpaRepository _userJpaRepository;

    @Override
    public UserEntity save(UserEntity userEntity) {
        return _userJpaRepository.save(userEntity);
    }

    @Override
    public Optional<UserEntity> findById(long id) {
        return _userJpaRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByIdAndStatus(long id, UserStatus userStatus) {
        return _userJpaRepository.findByIdAndStatus(id, userStatus);
    }

    @Override
    public Optional<UserEntity> findByEmailAndStatus(String email, UserStatus userStatus) {
        return _userJpaRepository.findByEmailAndStatus(email, userStatus);
    }
}
