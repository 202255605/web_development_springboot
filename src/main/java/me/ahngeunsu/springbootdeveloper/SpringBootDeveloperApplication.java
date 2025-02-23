package me.ahngeunsu.springbootdeveloper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringBootDeveloperApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootDeveloperApplication.class, args);
    }
}
/*
    사전 지식 : OAuth
        - 제 3의 서비스에 계정 관리를 맡기는 방식. naver/Google/kakao로 로그인하기와 같은 예시

        관련 용어 정리
            리소스 오너(Resource Owner) : 인증 서버에 자신의 정보를 사용하도록 허용하는 주체.
                서비스를 이용하는 사용자가 리소스 오너에 해당.
            리소스 서버(Resource Server) : 리소스 오너의 정보를 가지며, 리소스 오너의 정보를
                보호하는 주체를 의미하고, 네이버, 구글, 카카오, 페이스북이 리소스 서버에 해당.
            인증 서버(Authorization Server) : 클라이언트에게 리소스 오너의 정보에 접근할 수 있는
                토큰을 발급하는 역할을 하는 애플리케이션을 의미
            클라이언트 애플리케이션(Client Application) : 인증 서버에게 인증을 받고 리소스 오너의
                리소스를 사용하는 주체를 의미.

        OAuth를 사용하면 인증 서버에서 발급받은 토큰을 사용해서 리소스서버에
            리소스오너의 정보를 요청하고 응답 받아 사용 가능.
            그렇다면 리소스오너의 정보를 어떻게 취득할 수 있는지에 대한 방법이 필요.

            리소스 오너 정보를 취득하는 네 가지 방법
                1) 권한 부여 코드 승인 타입(Authorization Code Grant Type) :
                    OAuth 2.0에서 가장 대중적인 인증 방법. 클라이언트가 리소스에 접근하는 데 사용.
                    권한에 접근할 수 있는 코드와 리소스 오너에 대한 액세스 토큰을 발급받는 방식.

                2) 암시적 승인(Implicit Grant Type) :
                    서버가 없는 자바스크립트 웹 애플리케이션 클라이언트에서 주로 사용.
                    클라이언트가 요청을 보내면 리소스 오너의 인증 과정 이외에는 권한 코드 교환 등의
                    별다른 인증 과정을 거치지 않고 액세스 토큰을 제공 받는 형식

                3) 리소스 소유자 암호 자격증명 승인 타입(Resource Owner Password Credentials) :
                    클라이언트의 패스워드를 이용해서 액세스 토큰에 대한 사용자의 자격 증명을 교환하는 방식

                4) 클라이언트 자격 증명 승인 타입(Client Credentials Grant) :
                    클라이언트가 컨텍스트 외부에서 액세스 토큰을 얻어 특정 리소스에 접근을 요청할 때 사용하는 방식

                권한 부여 코드 승인 타입이란?
                    애플리케이션 / 리소스 오너(사용자) / 리소스 서버 / 인증 서버가 어떤 순서로 인증을 하는지 알 필요가 있습니다.

                                3. 인증 코드 발급
                애플리케이션 ----------------------------→ 인증 서버
                    ↑   \      4. 액세스 토큰으로 발급         ↑
                    |    \                                  |
                    |     \                                 |
                    |      \                                |
     1. 권한 요청    |       5. 액세스 토큰으로 데이터에 접근    | 인증 서버에
                    |                              \        | 인증 작업 위임
                    |                               \       |
                    |                                \      |
                    |                                 \     |
                    |                                   ↘   |
                리소스 오너 ←---------------------------- 리소스 서버
                              2. 데이터 접근용 권한 부여


          1. 권한 요청 :
            권한 요청은 클라이언트, 즉 스프링 부트 서버가 특정 사용자 데이터에 접근하기 위해 권한 서버,
            즉 카카오나 구글 권한 서버에 요청을 보내는 거라고 볼 수 있습니다. 요청 URI는 권한 서버마다 다르지만,
            보통은 클라이언트ID, 리다이렉트 URI, 응답 타입 등을 파라미터로 보냅니다.
            실제 요청에 쓰이는 요청 URI 예시를 통해 주요 파라미터를 소개하자면,

                🍎권한 요청을 위한 파라미터 예
                GET spring-authorization-server.example/authorize?
                    client_id=66a36b4c2&
                    redirect_uri=http://localhost:8080/myapp&
                    response_type=code&
                    scope=profile

           client_id : 인증 서버가 클라이언트에 할당한 고유 식별자.
            이 값은 클라이언트 애플리케이션을 OAuth 서비스에 등록할 때 서비스에서 생성하는 값

           redirect_uri : 로그인 성공시 이동해야하는 URI
           response_type : 클라이언트가 제공받길 원하는 응답 타입. 인증 코드를 받을 때는 code값을 포함해야 합니다.
           scope : 제공받고자 하는 리소스 오너의 정보 목록

       2. 데이터 접근용 권한 부여
        인증 서버에 요청을 처음 보내는 경우 사용자에게 보이는 페이지를 로그인 페이지로 변경하고
        사용자의 데이터에 접근 동의를 얻습니다. 이 과정은 최초 1회만 진행.
        이후에는 인증 서버에서 동의 내용을 저장하고 있기 때문에 로그인만 진행.
        로그인 성공하면 권한 부여 서버는 데이터에 접근할 수 있게 인증 및 권한 부여를 수신

       3. 인증 코드 제공
        사용자가 로그인에 성공하면 권한 요청 시에 파라미터로 보낸 redirect_uri로 리다이렉션됩니다.
        이때 파라미터에 인증 코드를 함께 제공합니다.

        🎈 인증 코드 예시
        GET http://localhost:8080/myapp?code=als2f3mcj2

       4. 액세스 코드 응답
        '인증 코드'를 받으면 '액세스 토큰'으로 교환해야 합니다.
        액세스 토큰은 로그인 세션에 대한 보안 자격을 증명하는 식별 코드를 의미.
        보통 다음과 같이 /token POST 요청을 보냅니다.

        🎈 /token POST 요청 예시
        {
            "client_id": "66a36b4c2",
            "client_secret": "aabb11dd44",
            "redirect_uri": "http://localhost:8080/myapp",
            "grant_type": "authorization_code",
            "code": "a1b2c3d4e5f67h8"
        }
                client_secret : OAuth 서비스에 등록할 때 제공받는 비밀키

                grant_type : 권한 유형을 확인하는 데 사용. 이 때는 authorization_code로 설정해야 함. 권한 서버는 요청 값을 기반으로
                    유효한 정보인지 확인하고, 유효한 정보라면 액세스 토큰으로 응답합니다.

                🍎액세스 토큰 응답 값의 예
                {
                    "access_token": "aasdffb",
                    "token_type": "Bearer"
                    "expires_in": 3600,
                    "scope": "openid profile"
                ...생략...
                }

            5. 액세스 토큰으로 API 응답 & 반환
                이제 제공받은 액세스 토큰으로 리소스 오너의 정보를 가져올 수 있습니다. 정보가 필요할 때마다 API 호출을 통해 정보를 가져오고
                리소스 서버는 토큰이 유효한지 검사한 뒤에 응답합니다.

                🍎리소스 오너의 정보를 가져오기 위한 요청 예
                    GET spring-authorization-resource-server .example.com/userinfo
                    Header: Authorization: Bearer aasdffb

                여기까지가 권한 부여 코드 승인 타입의 흐름입니다. 대부분의 OAuth를 구현한 라이브러리는 이 흐름을 바탕으로 코드를 구현하기 때문에
                흐름을 이해하고 넘어가는 것이 좋습니다.

        쿠키란?
            사용자가 어떤 웹사이트를 방문했을 때 해당 웹사이트의 서버에서 여러분의 로컬 환경에 저장하는 작은 데이터를 의미함. 이 값이 있기 때문에
            이전에 방문한 적이 있는지 알 수 있고, 이전에 로그인을 했다면 로그인 정보도 유지할 수 있는 것입니다. 쿠키는 키와 값으로 이루어져 있으며,
            만료 기간, 도메인 등의 정보를 가지고 있습니다. HTTP 요청을 통해 쿠키의 특정 키에 값을 추가할 수 있습니다.

                흐름                  GET /members
                    1. 브라우저에서 요청 -------------> 2. 서버에서 쿠키 설정
                                                            |
                                                            ↓   Set-Cookie: member_id=1
                     4. GET /members  <--------------- 3. 브라우저에 쿠키 저장
                        Cookie: member_id = 1


    쿠키 관리 클래스를 구현할겁니다.
        OAuth2 인증 플로우를 구현하면서 쿠키를 사용할 일이 생깁니다.
        그때마다 쿠키를 생성하고 삭제하는 로직을 추가하면 불편해서
        유틸리티로 사용할 쿠키 관리 클래스를 미리 구현

        springbootdeveloper 패키지 내에 util 패키지를 생성 -> CookieUtil.java를 생성




        자 현재 access_token을 기준으로 합니다 -> 저희는 지금 oauth를 통해서 로그인을 했습니다.
        access_token과 refresh_token을 받았습니다.

        -> 로그인 이전에 이미 data.sql을 통해서 1-3번글까지는 있는 상황 각각 user 1 - user 3

        즉, 현재 우리가 로그인한 상태로는 새로운 글 생성 및, 새로 생성된 글을 바탕으로 한
        수정, 및 삭제가 가능합니다.
 */

/*

결국 로그인 기능을 생각해보면 다음과 같이 한판 정리가 가능하다.

A. 최초 로그인 시:

사용자가 보호된 리소스 접근 시도
SecurityConfig가 /login으로 리다이렉트
Config 파일에 정의되어 있듯이 , 쿠키에 자신의 인증 정보를 담아 소셜 로그인 제공 기관으로 가서 본인 인증을 완료하고 그 기관이 내어주는 나의 정보를 다시 쿠키에 담아 가져온다
우리가 받은 해당 정보를 OAuth2UserCustomService 에 적힌 대로 업데이트를 하던 아니면 없는 고객이라면 우리 db에 추가를 하던 조치를 취한다.
OAuth2 로그인 성공 시 SuccessHandler가 JWT 토큰 발급

즉 TokenAuthenticationFilter을 들를 일이 없음 토큰이 없으니까

B. 이후 API 요청 시:

SecurityConfig가 TokenAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 위치시켜놨음 그러므로
모든 요청은 TokenAuthenticationFilter를 먼저 통과 , 토큰이 있으니!

TokenAuthenticationFilter로 갔을때 -> filter 결과 토큰의 유효성 인정 -> Authentication을 받음 -> SecurityContextHolder.getContext().setAuthentication(authentication);
을 통해 해당 client는 자신의 프론트 엔드에 유효한 토큰(Authentication 자체? )을 저장해 놓음 -> 다른 요청을 할때 마다 즉 요청하는 url이 바뀔때 마다 그 클라이언트는 filter에 가게 되고 그 filter에서 당당하게 토큰을 보여주고 또 setAuthentication(authentication)하게 된다.
토큰이 유효하면 SecurityContext에 인증 정보 설정 , 이후 필터들은 이미 설정된 인증 정보를 사용

C. 리프레시 토큰의 작동까지 우리의 프로젝트에 있는지는 모르겠지만 우선 리프레시 토큰의 작동 방식부터 알아야 한다.

OAuth2 로그인 성공
액세스 토큰 생성 (OAuth2SuccessHandler)
리프레시 토큰 생성 및 DB 저장 (OAuth2SuccessHandler) -> Repository , Domain에  RefreshTokenRepository만 있는 이유
두 토큰 모두 클라이언트에 전달 (OAuth2SuccessHandler -> RefreshToken은 cookie로 , AccessToken은 redirect url에 담아서)


액세스 토큰 만료 시:

클라이언트가 리프레시 토큰으로 재발급 요청
리프레시 토큰 유효성 검증
새로운 액세스 토큰 발급
리프레시 토큰은 그대로 유지

리프레시 토큰 만료 시:

리프레시 토큰 만료 확인
DB에서 토큰 삭제
재로그인 필요 응답

리프레시 토큰은 DB에 저장되어 관리됨
토큰 탈취 시 서버에서 리프레시 토큰 삭제로 대응 가능

cf> 너의 claude 3.5 sonnet 에 SpringBoot 인증 시스템 구조에 대한 이해를 돕는 그림들이 많이 있다 꼭 언젠가 볼 수 있도록

 */