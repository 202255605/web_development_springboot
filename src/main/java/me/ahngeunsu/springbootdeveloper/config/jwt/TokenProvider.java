package me.ahngeunsu.springbootdeveloper.config.jwt;

// TokenProvider : 토큰의 생성 , 파싱 , 유효성 검증

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public boolean validToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token); // 내용의 검증과 추출 즉 토큰의 서명 검증 , 토큰의 형식 검증 , 토큰의 만료여부 검증 , 페이로드(내용 부분 검증)
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 권한 설정
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token); // 토큰의 body 즉 페이로드를 받아옴
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_USER")); //사용자 권한 설정 - "ROLE_USER" 하나만 가진 Set 생성

        Map<String, Object> attributes = new HashMap<>();
        // claims.getSubject()로 얻은 사용자 식별자를 "email" 키로 저장
        attributes.put("email", claims.getSubject());

        // 4. OAuth2User 객체 생성
        OAuth2User oauth2User = new DefaultOAuth2User(
                authorities,      // 사용자 권한 정보
                attributes,       // 사용자 속성 정보를 담은 Map
                "email"            // 사용자 식별을 위해 사용할 속성의 키 이름
        );

        // 5. OAuth2 인증 토큰 생성 및 반환
        return new OAuth2AuthenticationToken(
                oauth2User,       // OAuth2User 타입의 Principal
                authorities,      // 권한 정보
                "google"         // OAuth 제공자 ID (예: google, github 등)
        );
    }
    // Set은 Java의 컬렉션 자료형 중 하나 -> Set<>의 <> 안에 있는 자료형들의 데이터들이 많이 모여있는 집합 자료형이라고 볼 수 있겠다.

//        return new UsernamePasswordAuthenticationToken(
//                new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities),
//                token, authorities); 원래의 코드 ->
        /*
        여기서 new org.springframework.security.core.userdetails.User(...) 부분이 Principal을 User 객체로 설정하는 부분입니다. UsernamePasswordAuthenticationToken의 첫 번째 파라미터가 Principal이 되는데, 여기서 User 객체를 생성해서 넣고 있죠.
        그래서 나중에 Controller에서 OAuth2User로 캐스팅하려고 할 때 문제가 발생하는 것입니다. Principal이 User 타입이기 때문에 OAuth2User로 캐스팅할 수 없는 거죠.

        org.springframework.security.core.userdetails.User는: UserDetails 인터페이스를 구현한 클래스입니다
        OAuth2User와는 다른 인터페이스를 구현하기 때문에 직접적인 캐스팅은 불가능합니다

        쨋든
        getAuthentication 메서드의 리턴값을 User가 아닌 OAuth2User 타입으로 여기서 부터 맞춰주면 나중에 /articles 있는 BlogViewController 캐스팅이 이루어지지 않아서 authentication의 principal의 값이 꼬이는 문제는 발생하지 않겠네

         */


    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }


    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}