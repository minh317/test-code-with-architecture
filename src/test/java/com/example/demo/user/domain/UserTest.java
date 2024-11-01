package com.example.demo.user.domain;

import com.example.demo.common.exception.CertificationCodeNotMatchedException;
import com.example.demo.user.service.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserTest {

    @Autowired
    UserRepository _userRepository;

    @DisplayName("User는 UserCreate 객체로 생성할 수 있다.")
    @Test
    void creatableUserWithUserCreate() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .nickname("bob")
                .address("서울시 강남구")
                .email("bob@gmail.com")
                .build();

        User user = User.from(userCreate);

        // when
        User savedUser = _userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo(userCreate.getNickname());
        assertThat(savedUser.getAddress()).isEqualTo(userCreate.getAddress());
        assertThat(savedUser.getEmail()).isEqualTo(userCreate.getEmail());
    }

    @DisplayName("User는 UserUpdate 객체로 수정할 수 있다.")
    @Test
    void updatableUserWithUserUpdate() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .nickname("bob")
                .address("서울시 강남구")
                .email("bob@gmail.com")
                .build();

        User user = User.from(userCreate);

        User savedUser = _userRepository.save(user);

        UserUpdate userUpdate = UserUpdate.builder()
                .nickname("alice")
                .address("서울시 강북구")
                .build();

        // when
        User updatedUser = _userRepository.save(savedUser.update(userUpdate));

        // then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getNickname()).isEqualTo(userUpdate.getNickname());
        assertThat(updatedUser.getAddress()).isEqualTo(userUpdate.getAddress());
        assertThat(updatedUser.getEmail()).isEqualTo(userCreate.getEmail());
    }

    @DisplayName("User는 로그인 할 수 있고 로그인 시 마지막 로그인 시간이 변경된다.")
    @Test
    void signInUserAndUpdateLastLoginAt() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .nickname("bob")
                .address("서울시 강남구")
                .email("bob@gmail.com")
                .build();

        User user = User.from(userCreate);

        User savedUser = _userRepository.save(user);
        log.info("loginAt: {}", savedUser.getLastLoginAt());

        // when
        User loginUser = _userRepository.save(savedUser.login());
        log.info("loginAt: {}", loginUser.getLastLoginAt());

        // then
        assertThat(loginUser.getLastLoginAt().longValue()).isGreaterThan(0);
    }

    @DisplayName("유효한 인증 코드로 계정을 활성화 할 수 있다.")
    @Test
    void activateWithCorrectCertificationCode() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .nickname("bob")
                .address("서울시 강남구")
                .email("bob@gmail.com")
                .build();

        User user = User.from(userCreate);
        final String certificationCode = user.getCertificationCode();

        User savedUser = _userRepository.save(user);

        // when
        User certificatedUser = savedUser.certificate(certificationCode);
        log.info("certificationCode : {}", certificationCode);

        // then
        assertThat(certificatedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @DisplayName("잘못 된 인증 코드로 계정을 활성화 하려고 하면 에러가 발생한다.")
    @Test
    void test() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .nickname("bob")
                .address("서울시 강남구")
                .email("bob@gmail.com")
                .build();

        User savedUser = _userRepository.save(User.from(userCreate));
        final String invalidCertificationCode = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() -> savedUser.certificate(invalidCertificationCode))
                .isInstanceOf(CertificationCodeNotMatchedException.class)
                .hasMessage("자격 증명에 실패하였습니다.");
    }
}