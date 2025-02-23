package me.ahngeunsu.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.User;
import me.ahngeunsu.springbootdeveloper.dto.AddUserRequest;
import me.ahngeunsu.springbootdeveloper.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Long save(AddUserRequest dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return userRepository.save(User.builder()
                        .email(dto.getEmail())
                        .password(encoder.encode(dto.getPassword())) // password에는 crypt화 시켜서 저장해야지 그래그래
                .build()).getId();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("unexpected user"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("unexpected user"));
    }
}

// config 파일에서 oAuth2UserCustomService 이 loadUser을 거쳐 반환하는 User객체에 대해 , UserService를 실행
// 