package me.ahngeunsu.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname; // -> 빌더에 해당 필드 추가

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }
    // 밑의 문장에서 위의 문장의 순서로 이 구문의 의미를 풀어내보겠다 우선 반환형은 그냥 GrantedAuthority라는 인터페이스를 구현하는 또는 GrantedAuthority클래스를 상속하는
    // 자료형들을 반환 타입으로 가지는 getAuthorities 라는 함수를 정의하고 있는데 이때 반환형 즉 return값을 좀 더 깊게 보자면
    // SimpleGrantedAuthority 라는 클래스의 인스턴스 이면서 "users"라는 권한 문자열을 가지는 인스턴스 들로 이루어진 불변 리스트를 반환한다

    @Override
    public String getUsername() {
        return email;
    } // 아 롬복을 쓸 수가 없네 기본 필드와 이름은 같아도 명칭은 다르니까

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    } // 뭐지 ? User.java 까지 참조할 정도까지 프로그램이 전개 되었다는 건 무조건 token is not expired ,token is not locked 이런 뜻인가.

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 사용자 이름 변경
    public User update(String nickname) {
        this.nickname = nickname;

        return this;
    }
    /*
        리소스 서버에서 보내주는 사용자 정보를 불러오는 메서드 -> loadUser()
        -> users 테이블에서 사용자 정보가 있다면 이름을 업데이트
        -> 없다면 saveOrUpdate() 메서드를 통해서 users 테이블에 회원 데이터
        추가 예정

        config 패키지 -> oauth 패키지 생성 -> OAuth2UserCustomService.java
     */
}

