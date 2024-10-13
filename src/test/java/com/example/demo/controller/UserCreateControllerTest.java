package com.example.demo.controller;

import com.example.demo.model.dto.UserCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql("/sql/user-controller-test-data.sql")
class UserCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper _objectMapper = new ObjectMapper();

    @MockBean
    private JavaMailSender _javaMailSender;

    @DisplayName("유저를 생성할 수 있다.")
    @Test
    void test() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("minh319@naver.com")
                .nickname("bobpool")
                .address("jeju")
                .build();

        BDDMockito.doNothing().when(_javaMailSender).send(any(SimpleMailMessage.class));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("minh319@naver.com"))
                .andExpect(jsonPath("$.nickname").value("bobpool"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}