package me.ahngeunsu.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.RefreshToken;
import me.ahngeunsu.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.ahngeunsu.springbootdeveloper.dto.SaveRefreshTokenToDB;
import me.ahngeunsu.springbootdeveloper.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken save(SaveRefreshTokenToDB dto){ // 아니 외부에서 refreshTokenService.save(dto) 구문을 써야 하는데 이게 private이면 되겠어!@
        return refreshTokenRepository.save(RefreshToken.builder()
                .user_id(dto.getUserId())
                .refreshToken(dto.getRefreshToken())
                .build());
    } // 이 메서드의 입력 인자는 RefreshToken refreshToken 이어야 한다 -> 밑의 메서드 정의처럼 String refreshToken 을 인자로 넣으면 안된다는 말씀 -> BlogRepository에서 자기가 받아들이는 인자의 타입이 String형이 아니고 refreshToken 이라는 엔티티 타입이니까

    // 제공하는 2번째 service -> refreshtoken을 인자로 넣은 findByRefreshToken method를 기반으로 id , user_id , refresh_token 이 3개의 값을 불러온다.
    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("unexpected token"));
    }

}
