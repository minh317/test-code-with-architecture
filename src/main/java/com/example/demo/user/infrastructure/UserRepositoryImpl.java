package com.example.demo.user.infrastructure;

import com.example.demo.user.domain.User;
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
    public Optional<User> findById(long id) {
        return _userJpaRepository.findById(id).map(UserEntity::toModel);
    }

    @Override
    public Optional<User> findByIdAndStatus(long id, UserStatus userStatus) {
        return _userJpaRepository.findByIdAndStatus(id, userStatus).map(UserEntity::toModel);
    }

    @Override
    public Optional<User> findByEmailAndStatus(String email, UserStatus userStatus) {
        return _userJpaRepository.findByEmailAndStatus(email, userStatus).map(UserEntity::toModel);
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = UserEntity.fromModel(user);
        return _userJpaRepository.save(userEntity).toModel();
    }
}
