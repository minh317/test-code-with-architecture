package com.example.demo.controller;

import com.example.demo.model.UserStatus;
import com.example.demo.model.dto.UserUpdateDto;
import com.example.demo.repository.UserEntity;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql("/sql/user-controller-test-data.sql")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository _userRepository;

    @DisplayName("특정 ID의 유저 정보를 조회할 수 있다.")
    @Test
    void getUserWithExistId() throws Exception {
        // given
        final long id = 10L;

        // when & then
        MvcResult mvcResult = mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("minh317@naver.com"))
                .andExpect(jsonPath("$.nickname").value("bob"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(status().isOk())
                .andReturn();

        log.info("mvcResult: {}", mvcResult.getResponse().getContentAsString());
    }

    @DisplayName("존재하지 않는 유저의 ID는 조회할 수 없다.")
    @Test
    void getUserWithNotExistId() throws Exception {
        // given
        final long id = 100000L;

        // when & then
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Users에서 ID 100000를 찾을 수 없습니다."));
    }

    @DisplayName("유저의 인증 코드를 토대로 PENDING -> ACTIVE 상태로 변경할 수 있다.")
    @Test
    void verifyWithCorrectCertificationCode() throws Exception {
        // given
        final long id = 11L;
        final String certificationCode = "abcdefgh-ijkl-mnop-qrst-uvwxyzabcdeg";

        // when
        mockMvc.perform(get("/api/users/{id}/verify", id)
                    .queryParam("certificationCode", certificationCode))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:3000"));

        UserEntity user = _userRepository.getById(id);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @DisplayName("유저의 인증 코드가 올바르지 않으면 상태를 변경할 수 없다.")
    @Test
    void verifyWithInvalidCertificationCode() throws Exception {
        // given
        final long id = 11L;
        final String certificationCode = "abcdefgh-ijkl-mnop-qrst-abababddd";

        // when & then
        mockMvc.perform(get("/api/users/{id}/verify", id)
                        .queryParam("certificationCode", certificationCode))
                .andExpect(status().isForbidden())
                .andExpect(content().string("자격 증명에 실패하였습니다."));
    }

    @DisplayName("User의 닉네임을 변경할 수 있다.")
    @Test
    void updateUserNicknameWithCorrectEmail() throws Exception {
        // given
        final long id = 10L;
        final String email = "minh317@naver.com";

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .nickname("pool")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        // when
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("EMAIL", email)
                        .content(objectMapper.writeValueAsBytes(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value("pool"));

        UserEntity user = _userRepository.getById(id);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo("pool");
    }

    @DisplayName("User의 주소를 변경할 수 있다.")
    @Test
    void updateUserAddressWithCorrectEmail() throws Exception {
        // given
        final long id = 10L;
        final String email = "minh317@naver.com";

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .address("Seoul")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        // when
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("EMAIL", email)
                        .content(objectMapper.writeValueAsBytes(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.address").value("Seoul"));

        UserEntity user = _userRepository.getById(id);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAddress()).isEqualTo("Seoul");
    }

    @DisplayName("잘못된 이메일을 입력하면 User의 닉네임을 변경할 수 없다.")
    @Test
    void canNotUpdateUserNicknameWithInvalidEmail() throws Exception {
        // given
        final String email = "minh317@invalid.email.com";

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .nickname("pool")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("EMAIL", email)
                        .content(objectMapper.writeValueAsBytes(userUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("Users에서 ID %s를 찾을 수 없습니다.", email)));
    }
}