package me.ahngeunsu.springbootdeveloper.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveRefreshTokenToDB {
    private String refreshToken;
    private String userId;
}
