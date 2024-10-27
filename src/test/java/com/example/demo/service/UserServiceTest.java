package com.example.demo.service;

import com.example.demo.common.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserUpdate;
import com.example.demo.user.infrastructure.UserEntity;
import com.example.demo.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@Slf4j
@SpringBootTest
@TestPropertySource("classpath:test-application.yml")
@Transactional
@Sql(value = "/sql/user-service-test-data.sql")
class UserServiceTest {

    @Autowired
    UserService _userService;

    @MockBean
    JavaMailSender _javaMailSender;

    @DisplayName("이메일로 User를 조회할 때는 PENDING 상태인 User는 조회하지 않는다.")
    @Test
    void canNotFindPendingUserWithEmail() {
        // given
        final String email = "minh318@naver.com";

        // when & then
        assertThatThrownBy(() -> _userService.getByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Users에서 ID %s를 찾을 수 없습니다.", email));
    }

    @DisplayName("이메일로 User를 조회할 때는 ACTIVE 상태인 User만 조회한다.")
    @Test
    void canFindActiveUserWithEmail() {
        // given
        final String email = "minh317@naver.com";

        // when
        UserEntity user = _userService.getByEmail(email);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @DisplayName("ID로 User를 조회할 때는 PENDING 상태인 User는 조회하지 않는다.")
    @Test
    void canNotFindPendingUserWithId() {
        // given
        final long id = 2L;

        // when & then
        assertThatThrownBy(() -> _userService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Users에서 ID %s를 찾을 수 없습니다.", id));
    }

    @DisplayName("ID로 User를 조회할 때는 ACTIVE 상태인 User만 조회한다.")
    @Test
    void canFindActiveUserWithId() {
        // given
        final long id = 10L;

        // when
        UserEntity user = _userService.getById(id);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
    }

    @DisplayName("User를 생성할 수 있다.")
    @Test
    void create() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .email("minh319@naver.com")
                .address("Seoul")
                .nickname("bob")
                .build();

        // when
        UserEntity user = _userService.create(userCreate);
        BDDMockito.doNothing().when(_javaMailSender).send(any(SimpleMailMessage.class));

        // then
        assertThat(user.getId()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @DisplayName("User를 수정할 수 있다.")
    @Test
    void update() {
        // given
        UserUpdate userUpdate = UserUpdate.builder()
                .address("Seoul")
                .nickname("bob-N")
                .build();

        // when
        UserEntity user = _userService.update(10L, userUpdate);

        // then
        assertThat(user.getId()).isNotNull();
        assertThat(user.getId()).isEqualTo(10L);
        assertThat(user.getAddress()).isEqualTo("Seoul");
        assertThat(user.getNickname()).isEqualTo("bob-N");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @DisplayName("로그인 할 수 있다.")
    @Test
    void login() {
        // given
        final long id = 10L;

        // when
        _userService.login(id);
        UserEntity user = _userService.getById(id);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getLastLoginAt()).isGreaterThan(0);
    }

    @DisplayName("잘못 된 인증 코드는 ACTIVE 상태로 변경할 수 없다.")
    @Test
    void invalidCertificationCode() {
        // given
        final long id = 11L;
        final String certificationCode = "abcdefgh-ijkl-mnop-qrst";

        // when & then
        assertThatThrownBy(() -> _userService.verifyEmail(id, certificationCode))
                .isInstanceOf(CertificationCodeNotMatchedException.class);
    }

    @DisplayName("PENDING 상태의 사용자는 인증 코드를 통해 ACTIVE 상태로 변경할 수 있다.")
    @Test
    void correctCertificationCode() {
        // given
        final long id = 11L;
        final String certificationCode = "abcdefgh-ijkl-mnop-qrst-uvwxyzabcdeg";

        // when
        _userService.verifyEmail(id, certificationCode);
        UserEntity user = _userService.getById(id);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getCertificationCode()).isEqualTo(certificationCode);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

}