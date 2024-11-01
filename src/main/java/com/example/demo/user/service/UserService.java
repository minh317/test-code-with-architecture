package com.example.demo.user.service;

import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserUpdate;
import com.example.demo.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository _userRepository;
    private final CertificationService _certificationService;

    public User getByEmail(String email) {
        return _userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Users", email));
    }

    public User getById(long id) {
        return _userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Users", id));
    }

    @Transactional
    public User create(UserCreate userCreate) {
        User user = User.from(userCreate);
        user = _userRepository.save(user);

        _certificationService.send(userCreate.getEmail(), user.getId(), user.getCertificationCode());
        return user;
    }

    @Transactional
    public User update(long id, UserUpdate userUpdate) {
        User user = getById(id);
        return _userRepository.save(user.update(userUpdate));
    }

    @Transactional
    public void login(long id) {
        User user = _userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Users", id));
        _userRepository.save(user.login());
    }

    @Transactional
    public void verifyEmail(long id, String certificationCode) {
        User user = _userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Users", id));
        user = user.certificate(certificationCode);

        _userRepository.save(user);
    }
}