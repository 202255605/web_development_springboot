package me.ahngeunsu.springbootdeveloper.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt") //application.yml에 적혀있는 정보 즉 외부 정보를 자바객체, 여기선 JwtProperties.java의 인자들에 매핑하는 것
public class JwtProperties {
    private String issuer;
    private String secretKey;
}
