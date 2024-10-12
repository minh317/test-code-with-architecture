package com.example.demo.repository;

import com.example.demo.model.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestPropertySource("classpath:test-application.yml")
@Sql("/sql/user-repository-test-data.sql")
class UserRepositoryTest {

    @Autowired
    private UserRepository _userRepository;

    @AfterEach
    void tearDown() {
        _userRepository.deleteAllInBatch();
    }

    @DisplayName("유저의 아이디와 상태 값을 토대로 유저를 조회할 수 있다.")
    @Test
    void findByIdAndStatusTest() {
        // given
        final long id = 1L;

        // when
        Optional<UserEntity> findUser = _userRepository.findByIdAndStatus(id, UserStatus.ACTIVE);

        // then
        assertThat(findUser.isPresent()).isTrue();
    }

    @DisplayName("유저의 이메일과 상태 값을 토대로 유저를 조회할 수 있다.")
    @Test
    void findByEmailAndStatusTest() {
        // given
        final String email = "minh317@naver.com";

        // when
        Optional<UserEntity> optionalFindUser = _userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);
        UserEntity findUser = optionalFindUser.get();

        // then
        assertThat(findUser).isNotNull();
        assertThat(findUser.getEmail()).isEqualTo(email);
    }

    @DisplayName("존재하지 않는 유저 ID를 조회하려고 하면 Optional.Empty를 반환한다.")
    @Test
    void findByIdAndStatusTestWithNotExistsUser() {
        // given
        final long id = -1L;

        // when
        Optional<UserEntity> optionalFindUser = _userRepository.findByIdAndStatus(id, UserStatus.ACTIVE);

        // then
        assertThat(optionalFindUser.isEmpty()).isTrue();
    }

    @DisplayName("유저 ID는 존재하지만, 적절하지 않은 상태로 조회하는 경우 Optional.Empty를 반환한다.")
    @Test
    void findByIdAndStatusTestWithNotExistsStatus() {
        // given
        final long id = 1L;

        // when
        Optional<UserEntity> optionalFindUser = _userRepository.findByIdAndStatus(id, UserStatus.PENDING);

        // then
        assertThat(optionalFindUser.isEmpty()).isTrue();
    }

}