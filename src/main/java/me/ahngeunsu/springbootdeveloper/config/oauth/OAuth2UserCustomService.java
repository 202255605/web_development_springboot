package me.ahngeunsu.springbootdeveloper.config.oauth;

// 소셜 로그인 기관에서 받아온 사용자에 관한 정보를 어떻게 처리할 것인가를 정의해놓은 file

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.User;
import me.ahngeunsu.springbootdeveloper.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service        // import org.springframework.stereotype.Service;
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override       // alt + insert / command + n눌러서 override한 메서드 -> 재정의
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest); //
        // OAuth2UserRequest는 소셜 로그인 기관에서 오는 사용자 정보 형식 -> DefaultOAuth2UserService.loadUser 을 통해 OAuth2User 타입으로 변환 -> 둘 다 springframework.security에 정의되어 있는 타입
        saveOrUpdate(user); // 바로 밑에 정의

        return user; // 이 user객체를 user
    }

    // 위에 빨간줄 뜨는 메서드 정의

    private User saveOrUpdate(OAuth2User oAuth2User) {
        // import me.ahngeunsu.springbootdeveloper.domain.User;
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");        // 다운캐스팅 예시 -> 특정 객체를 String형으로 다운캐스팅
        String name = (String) attributes.get("name");

        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name)) // 람다식 : 각 엔티티들에 대하여 이러한 행동을 수행한다. , update(name)은 User.java에 정의되어 있음 -> 이런 함수는 무조건 그 인스턴스의 nickname을 수정
                .orElse(User.builder()
                        .email(email)
                        .nickname(name)
                        .build());         // User 정보 없으면 (UserRepository에서 User 객체가 안 찾아지는 경우)  새로 저장 -> builder패턴으로 생성
        
        //  public User update(String nickname) {
        //        this.nickname = nickname;
        //
        //        return this;
        //    } --> findByEmail(email)의 결과로 return되는 것은 Optional<User>객체이고 , 그러면 .map(entity -> entity.update(name))이라고 쓰여진 구문의 의미는
        // 그렇게 가져온 user객체( db와 연결되니 이게 한쌍 한쌍이 entity이죠)에 대해서, 내가 빼온 그 entity에 대해 User.java에 정의되어 있는 .update라는 메서드를 실행하겠다 라는 의미



        return userRepository.save(user); //h2데이터베이스에 저장 (기반 전용 웹서버 : tomcat -> springbootframework 자동 제공)
    }
}
/*
    부모 클래스인 DefaultOAuth2UserService에서 제공하는 OAuth 서비스에서 제공하는 정보를 기반으로
    유저 객체를 만들어주는 loadUser() 메서드 사용 -> 객체를 불러왔습니다.(override했습니다)

    구글을 기준으로 했을 때 사용자 객체 내부는 (우리가 OAuth2UserRequest 객체로 받아와서 super.loadUser메서드를 이용해 변경한 OAuth2User 객체) wow!!
        식별자,
        이름
        이메일
        프로필 사진 링크 등의 정보 포함.

    saveOrUpdate() 메서드를 통해 사용자가 users 테이블에 존재하면 업데이트(혹시나 name이 바뀌었을 수도 있으니까 ),
    없으면 저장.(DB에 저장)

    OAuth2 설정 파일을 작성할 예정
        -> OAuth2를 사용한다는게 거의 기본적으로 JWT와 연계가 되는데
        기존 스프링 시큐리티를 구현하면서 작성한 설정이 아니라 추가적인 작업이 필요하다는 의미

        -> 기존의 폼 로그인 방식을 구현하려고 사용한 설정을 다 갈아엎어야 합니다.
        WebSecurityConfig.java를 싸그리 다 주석 처리 해둡니다.

        config 패키지 내에 WebOAuthSecurityConfig.java 파일 생성
 */

/*
 가장 위에 정의된 loadUser이라는 메서드의 기능을 한번 알아보자

loadUser 메소드:

정의부 -> public OAuth2User loadUser(OAuth2UserRequest userRequest)

OAuth2UserRequest: 소셜 로그인 제공자(ex) 구글, 페이스북 , 카카오톡 )로부터 받은 인증 요청 정보 (우리가 그런 로그인 제공자에게 넘긴 정보를 바탕으로 그 기관에서는 우리에 대한
본인인증이 끝나면 , 그 기관에서 보관중인 우리에 대한 정보를 웹사이트에 전송해줌 --> OAuth2 기능의 정의 ! )를 담고 있음

이 메소드는 DefaultOAuth2UserService 클래스의 메서드를 상속받아 오버라이드한 것

-> super.loadUser(userRequest):

부모 클래스(DefaultOAuth2UserService)의 loadUser 메소드를 호출 소셜 로그인 제공자로부터 사용자 정보를 가져옴 -> OAuth2UserRequest 객체가 곧 구글 이런 곳에서 우리가 받아온 우리의 정보
이 타입의 객체를 OAuth2User 객체로 반환 -> 우리가 이 OAuth2User객체를 이제부터 이용할 거거든

saveOrUpdate(user):

가져온 사용자 정보를 DB에 저장하거나 업데이트하는 메소드를 호출

 */

/*
<실제 OAuth2UserCustomService의 작동 과정을 보자면>

// Google이 제공하는 사용자 정보 형태
{
    "sub": "123456789",          // Google의 고유 식별자
    "name": "John Doe",          // 이름
    "email": "john@gmail.com",   // 이메일
    "picture": "https://...",    // 프로필 사진
    "email_verified": true       // 이메일 인증 여부
}

// 이를 우리 서비스의 User 엔티티로 변환 -> 구글에서 온 정보를 우리의 entity형태에 맞게 변환 (super.loadUser)의 역할
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String name;
    private String provider;      // "google", "facebook" 등
    private String providerId;    // OAuth2 제공자의 고유 식별자
}

이렇게 인증 요청에 대한 응답을 우리의 엔티티에 맞게 변환 한 그 형태(우리가 직접 변환하는 것이 어니라 super.loadUser이 알아서 변환해준다.)를 우리는 OAuth2User 자료형이라고 부르고 이 자료형에 대해 saveOrUpdate 실행

이런 OAuth2User 클래스의 인스턴스에 대해 .getAttributes() 라는 메서드를 우리가 이용할 수도 있고 , 그 attributes들에 대하여 .get("email") , .get("name") 등을 쓸 수 있다.



<질문>
Security Config 파일에 있는 .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService)) 이런 구문에 대해서 보면 oAuth2UserCustomService 라는건 OAuth2UserCustomService 의 인스턴스 인데 OAuth2UserCustomService에 정의되어 있는 어떤 메서드를 실행한다는 표현도 없어 
이게 config 파일의 특성상 그냥 특정 객체 타입의 인스턴스만 넣어놔도 그 안의 메서드들이 알아서 순차적으로 다 실행이 되는건가

<답>
이는 Spring Security의 설정 특성상 OAuth2UserCustomService의 인스턴스만 등록해도 프레임워크가 내부적으로 필요한 메서드들을 알아서 호출하는 구조입니다.
작동 방식을 자세히 설명하면:

OAuth2UserCustomService는 DefaultOAuth2UserService를 상속받고 있을 것이고, 이 클래스는 OAuth2UserService 인터페이스를 구현합니다.
Spring Security의 OAuth2 로그인 프로세스에서:

사용자가 소셜 로그인을 시도하면
프레임워크가 자동으로 등록된 OAuth2UserService 구현체를 찾아서
해당 서비스의 loadUser() 메서드를 호출합니다





 */




