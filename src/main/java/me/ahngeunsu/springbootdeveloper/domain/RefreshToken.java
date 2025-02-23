package me.ahngeunsu.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

//    public RefreshToken(Long userId, String refreshToken) {
//        this.userId = userId;
//        this.refreshToken = refreshToken;
//    }

    @Builder
    public RefreshToken(Long id , String user_id, String refreshToken) {
        this.id = id;
        this.userId = user_id;
        this.refreshToken = refreshToken;
    }

    public RefreshToken update(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
