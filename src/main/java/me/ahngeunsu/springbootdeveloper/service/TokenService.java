package me.ahngeunsu.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.config.jwt.TokenProvider;
import me.ahngeunsu.springbootdeveloper.domain.User;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public String createNewAccessToken(String refreshToken) {
        if(!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("unexpected token");
        }

        Long Id = refreshTokenService.findByRefreshToken(refreshToken).getId();
        User user = userService.findById(Id);

        return tokenProvider.generateToken(user, Duration.ofHours(2));
    }
}
