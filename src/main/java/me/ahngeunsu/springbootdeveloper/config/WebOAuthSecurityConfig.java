package me.ahngeunsu.springbootdeveloper.config;

// 세션 로그인(form로그인) 방식을 사용할 것이 아니기 때문에 설정 파일 , 다르게 말하면 모든 api 호출 관련 작업에서 우선적으로 호출되는 config파일에 대해서
// 세션 로그인 용 securityconfig 파일을 모두 버리고 토큰, OAuth2기능,JWT 이 기능들을 커버할 수 있는 config 파일로 새로 만든다.

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.config.jwt.TokenProvider;
import me.ahngeunsu.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import me.ahngeunsu.springbootdeveloper.config.oauth.OAuth2SuccessHandler;
import me.ahngeunsu.springbootdeveloper.config.oauth.OAuth2UserCustomService;
import me.ahngeunsu.springbootdeveloper.repository.RefreshTokenRepository;
import me.ahngeunsu.springbootdeveloper.service.RefreshTokenService;
import me.ahngeunsu.springbootdeveloper.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration  // import org.springframework.context.annotation.Configuration;
@Controller
public class WebOAuthSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;// ->   userInfoEndpoint.userService(oAuth2UserCustomService)) 여기의 userService와는 관련 없다! -> 이후 OAuth2SuccessHandler에 인지로 넘겨주기 위한 userService이다


    @Bean
    public WebSecurityCustomizer configure() {  // 스프링 시큐리티 기능 비활성화
// import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console; -> 세션에서도 이랬었지 H2Console , static folder 이하의 경로는 (web) -> web.ignoring()
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers(
                        new AntPathRequestMatcher("/img/**"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**")
                );

    }

    //web은 WebSecurityCustomizer의 파라미터입니다. 필요한 import들은 다음과 같습니다:
    // import org.springframework.security.config.annotation.web.builders.WebSecurity; -> 이렇게 import해주면 , (web) -> web.ignoring().requestMatchers(toH2Console()).requestMatchers(new AntPathRequestMathchers("/img/**")) 등등 의 코드 사용 가능

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. 토큰 방식으로 인증하기 때문에 기존에 사용하던 폼 로그인, 세션 비활성화
        return http
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 2. 헤더를 확인할 커스텀 필터 추가
                // tokenAuthenticationFilter는 좀 있다 정의할 예정입니다. -> 빨간줄 뜨는거 정상
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // 특정 필터 앞에 새로운 필터를 추가 ->  기존 필터 : UsernamePasswordAuthenticationFilter.class , 추가되는 필터 : tokenAuthenticationFilter() , 이걸 우리가 밑에 정의 함.
                // 3. 토큰 재발급 URL은 인증 없이 접근 가능하도록 설정. 나머지 API URL은 인증 필요
                .authorizeRequests(auth -> auth  // auth도 어디서 import되어 있는 정의되어 있는 용어 같은데
                        .requestMatchers(new AntPathRequestMatcher("/api/token")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
//                        .requestMatchers(new AntPathRequestMatcher("/articles/**")).authenticated() -> 사실 글 들은 아무나 봐도 되잖아 아무나 블로그에 들어와도 되고 대신 , 아무나 등록 하지 못 하게 하고 아무나 수정 및 삭제 하지 못 하게 하기 위해
//                        -> 그래서  /api 와 아닌 도메인을 분리 하는 듯
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2 // OAuth2 소셜 로그인 기능 활성화
                        .loginPage("/login") // 사용자가 인증되지 않은 상태에서 보호된 리소스에 접근하면 이 페이지로 리다이렉트(최초 로그인 시도 시)
                        // 4. Authorization 요청과 관련된 상태 저장
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                        // 이 파일 하단에 이 구문에 대한 자세한 설명 있음  : 간단히 보자면 : 프론트에서 백 쪽으로 엔드포인트 요청을 줬을때 클라이언트가 우리를 거쳐 소셜 로그인 기관으로 갔다가 다시 우리 사이트로 올 때까지의 과정에 인증요청정보를 Cookie에 담는 방식을 사용하겠다는 의미(token을 저장한다는 의미는 아님)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService))
                        // 소셜 로그인 제공기관에서 인증 정보 요청(Cookie에 담아져있겠지)에 대한 응답으로 사용자 정보가 도착하였을때 우리 서버는 그 사용자정보를 어떻게 처리할 것이가를 정의
                        // 우리가 받은 그 인증 정보를 OAuth2UserCustomService에 정의된 방식으로 처리 즉 받고 있는거라면 update만 , 우리의 서버에 없었다면 builder로 인스터스 빌드시켜서 우리의 서버에 새로 저장
                        // 5. 인증 성공시 실행할 핸들러
                        .successHandler(oAuth2SuccessHandler())
                )
                // 6. /api로 시작하는 url인 경우 401 상태 코드(HttpStatus.UNAUTHORIZED)를 반환하도록 예외 처리
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        /*.authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패시 invalid-token 페이지로 리다이렉트
                            response.sendRedirect("/invalid-token");
                        })*/)
                .build();
                // 바로 위에서 /api/** 의 url에 대한 권한 규칙이 코드로 정의 되어 있었고 여기서는 예외 처리 코드이다 -> 서로의 차이에 대한 설명이 밑에 적혀 있음.
    }

    // 바로 다음에 작성할 파일이므로 빨간줄 떠도 됩니다. (후에 OAuth2SuccessHandler 을 작성하고 import 시켜주면 됨.)
    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService,
                refreshTokenService
        );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    // 빨간줄 정상
    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository(){
        return new OAuth2AuthorizationRequestBasedOnCookieRepository(); // 이건 굳이 왜 정의함..그냥 바로 new OAuth2AuthorizationRequestBasedOnCookieRepository() 쓰면되지
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
/*
    OAuth2에 필요한 정보를 세션이 아니라 쿠키에 저장해 쓸 수 있도록 인증 요청과 관련된
    상태를 저장할 저장소를 구현합니다.

    config/oauth 내에
    OAuth2AuthorizationRequestBasedOnCookieRepository 클래스 생성.

 */

/*

토큰을 이용한 로그인 기능 구축을 위한 config 파일의 내용

: 세션기반 로그인 기능 비활성화 ->



1.

http
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 아마 이 코드들은 세션 기반으 로그인 과정을 비활성화 하기 위한 코드 들인 것 같은데 한번 설명해줄래'


이 코드 들에 대한 설명 -> Username-password 기반 filter-chain 앞에 tokenAuthentication-filter-chain 추가  ->

.csrf(AbstractHttpConfigurer::disable)

CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화
REST API에서는 CSRF 토큰이 필요 없으므로(stateless하기 때문에) 보통 비활성화 -> CSRF는 사용자가 세션을 통해 어떤 웹페이지에 접속해 있을때 그 세션을 이용하는 것이기에 필요 X

.httpBasic(AbstractHttpConfigurer::disable)

HTTP Basic 인증을 비활성화
Basic 인증은 username/password를 Base64로 인코딩하여 헤더에 포함시키는 방식
JWT를 사용할 것이므로 이 기능은 불필요(토큰을 서버에 받기 위해 진짜 맨 처음에 서버에 USERNAME, PASSWORD를 전송할때도 HEADER이 아닌 BODY를 이용)

.formLogin(AbstractHttpConfigurer::disable)

스프링 시큐리티의 기본 로그인 폼 비활성화
폼 로그인은 세션 기반이므로 JWT를 사용할 때는 비활성화

.logout(AbstractHttpConfigurer::disable)

스프링 시큐리티의 기본 로그아웃 기능 비활성화
JWT는 서버에서 세션을 관리하지 않으므로 별도의 로그아웃 처리가 필요 없음

.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

세션 생성 정책을 STATELESS로 설정
서버에서 세션을 아예 생성하지 않음
JWT 토큰으로만 인증하겠다는 의미

2.

 .authorizationEndpoint(authorizationEndpoint ->
                                    authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))

OAuth2 인증 과정에서 생성되는 인증 요청을 저장하는 방식을 설정 , 여기서는 쿠키에 인증 요청을 저장하도록 설정 , 인증 과정 중에 상태를 유지하기 위해 필요 (CSRF 방지 등)

우선 OAuth2 의  기본적인 흐름을 보자면

[사용자] -> [로그인 버튼 클릭]
    -> [인증 요청 정보를 쿠키에 저장]  -->  이 부분이 authorizationEndpoint 에 해당한다고 볼 수 있다.
    -> [소셜 로그인 페이지로 리다이렉트]
    -> [소셜 로그인 완료]
    -> [우리 서비스로 리다이렉트]
    -> [쿠키에서 원래 요청 정보를 읽어 검증]
    -> [인증 완료]

그리고  실제 쿠키를 생성해서 저장하는 과정까지의 로직을 보자면

// 예시: 사용자가 "Google로 로그인" 버튼을 클릭

// 1. 인증 요청 정보가 생성됨
OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.builder()
    .clientId("google-client-id")
    .redirectUri("http://your-app/oauth2/callback")
    .state("random-state-value")  // CSRF 방지용 상태 값 -> 랜덤 값 , 나중에 응답으로 온 값이 이 값과 같아야 oauth 로그인 제공자가 아닌 제 3의 공격자에게서 온 신호가 아니란걸 알 수 있다.
    .build();

// 2. 이 정보를 쿠키에 저장
Cookie cookie = new Cookie("oauth2_auth_request", serialize(authRequest));
response.addCookie(cookie);

실제 인증 요청 정보를 쿠키에 저장하는 과정을 뜯어보면 이렇다.



 .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService))

 */

/*
첫 번째 코드 (authorizeRequests)는 접근 권한 규칙을 정의합니다:

.authorizeRequests(auth -> auth
    .requestMatchers(new AntPathRequestMatcher("/api/token")).permitAll()  // 토큰 엔드포인트는 모두 허용
    .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated() // 다른 API는 인증 필요
    .anyRequest().permitAll())  // 나머지는 모두 허용

두 번째 코드 (exceptionHandling)는 인증 실패시 어떻게 응답할지를 정의합니다:

.exceptionHandling(exceptionHandling -> exceptionHandling
    .defaultAuthenticationEntryPointFor(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
        new AntPathRequestMatcher("/api/**")))

작동 과정을 예시로 설명하면:

인증되지 않은 사용자가 "/api/users"에 접근 시도 , 접근시도가 왔을때 "/api/token , .anyrequest() 모두 아니기에 접근 거부 , -> exceptionalhandling 로직으로 보냄"

즉:
"/api/로 시작하는 모든 URL에 대해서, 인증되지 않은 접근이 발생하면 로그인 페이지로 리다이렉트하지 말고 401 Unauthorized 상태 코드로 응답하겠다

특히 일반적인 웹 페이지처럼 로그인 페이지로 리다이렉트되지 않고, 대신 401 Unauthorized 상태 코드를 반환합니다.

이렇게 설정하는 이유는:

API는 보통 프론트엔드 애플리케이션이나 다른 서비스에서 호출하기 때문에, HTML 로그인 페이지를 보여주는 것이 적절하지 않습니다.
401 응답을 받은 클라이언트는 이를 통해 인증이 필요하다는 것을 프로그래밍 방식으로 인지하고 처리할 수 있습니다.
프론트엔드는 보통 이 401 응답을 받으면 자체적으로 로그인 페이지로 리다이렉트하거나 토큰 갱신 등의 처리를 수행합니다.

만약 일반적인 웹 페이지 접근의 경우라면:
.defaultAuthenticationEntryPointFor(
    new LoginUrlAuthenticationEntryPoint("/login"),
    new AntPathRequestMatcher("/web/**"))

이런 식으로 설정하여 로그인 페이지로 리다이렉트되게 할 수 있습니다

authorizeRequests: "누가 접근할 수 있는가?"를 정의
exceptionHandling: "접근이 거부됐을 때 어떻게 응답할 것인가?"를 정의

이렇게 두 코드는 서로 다른 역할을 하며, 함께 작동하여 보안 로직을 완성합니다.

 */

/*
사용자가 소셜 로그인 버튼 클릭 → 구글 로그인 페이지로 이동 → 다시 우리 서비스로 돌아오는 과정에서
방금 전에 보셨던 그 쿠키 저장소(CookieAuthorizationRepository)를 사용하겠다고 지정하는 거예요. : 쿠키 저장소를 사용하겠다 = 쿠키에 인증요청정보를 저장하겠다

1. 사용자가 소셜 로그인 버튼을 클릭했을때

// 쿠키에 저장되는 정보:
- 클라이언트 ID (예: Google에서 발급받은 ID)
- 리다이렉트 URI (로그인 완료 후 돌아올 우리 서비스의 주소)
- 인증 범위 (scope - 예: email, profile 등)
- 상태 토큰 (보안을 위한 임시 토큰)

2. 구글에서 클라이언트가 자신의 인증을 마치고 우리 사이트로 리다이렉트 되었을때

// 쿠키에서 꺼내서 확인하는 정보:
- 아까 저장해둔 상태 토큰이 맞는지 확인 (보안 체크)
- 원래 요청했던 리다이렉트 URI가 맞는지 확인
- 요청했던 인증 범위가 맞는지 확인

3. 1~2 의 과정 전체를 봄으로써 인증요청정보의 쿠키 속 저장의  작동법을 알아보자

1. 사용자: "구글 로그인" 버튼 클릭

2. 저장소: "oauth2_auth_request" 쿠키에 정보 저장
   - "이 사용자는 example.com/callback으로 돌아가야 해요"
   - "이메일과 프로필 정보를 요청했어요"
   - "상태 토큰은 abc123이에요"

3. [구글 로그인 진행]

4. 구글이 우리 서비스로 리다이렉트

5. 저장소: 아까 저장한 쿠키 확인
   - "응, 맞아요. abc123 토큰이 일치해요" (일시적인 보안을 위해 작용하는 상태토큰의 일치 확인 )
   - "example.com/callback이 맞네요"
 */


/*
    OAuth2에 필요한 정보를 세션이 아니라 쿠키에 저장해 쓸 수 있도록 인증 요청과 관련된
    상태를 저장할 저장소를 구현합니다.

    config/oauth 내에
    OAuth2AuthorizationRequestBasedOnCookieRepository 클래스 생성.

    일단 api가 들어면 무조건 맨 앞에는 TokenAuthenticationFilter 가 막고 있겠지 -> 그럼 api로 들어온 clinet의 프로트엔트(url)에 저장된 token을 뜯어서 tokenprovider.valid(token)으로 유효한 토큰을
    가지고 있는전지 확인 -> 유효한 토큰이면 authentication부여 -> 들어갈 수 있음
    또는 이런 경우도 있을 수가 있다.
    -> TokenAuthenticationFilter에서 validToken(token)이 false이므로 SecurityContext에 인증 정보가 설정되지 않은 상태로 다음 필터로 진행됩니다.
    WebSecurityConfig의 설정에 따라:

    "/api/**" 패턴의 URL은 .authenticated()로 설정되어 있어 인증이 필요합니다
    인증되지 않은 상태이므로 Spring Security의 기본 인증 메커니즘이 동작합니다
    .oauth2Login()에서 설정한 loginPage("/login")에 따라 로그인 페이지로 리다이렉트됩니다

    -> 어쨋든 SecurityConfig 파일에 정의되어 있듯이 , 로그인을 해야하는 상황에는 쿠키에 고객의 인증점보를 담아서 보내고 또 쿠키에서 소셜 로그인 기관이 보내온 고객의 개인정보를 꺼내서
    꺼내가지고 UserCustomService를 수행하고 이것까지 완벽하게 수행되면 OAuth2SuccessHandler 로 넘어가서 이게 수행되는거지

 */
/*
     이 file 에는 2개의 userService 가 있다 -> 하나는 우리가 Service 폴더 밑에 만들어 놓은 userService 파일이고 나머지 하나는 Spring Security OAuth2 Client의 OAuth2UserServiceEndpointConfig 인터페이스에 정의되어 있습니다.

    이 메서드는 Spring Security의 OAuth2 로그인 설정 과정에서 사용되며, 우리가 직접 구현하는 것이 아니라 Spring Security가 제공하는 메서드입니다. 우리는 이 메서드에 OAuth2UserService 인터페이스를 구현한 서비스(예: OAuth2UserCustomService)를 파라미터로 전달하여 사용합니다.
    이 메서드가 실제로 존재하는 경로는:
    org.springframework.security.config.oauth2.client.OAuth2LoginConfigurer.UserInfoEndpointConfig
    
    그 인자로써 UserCustomService를 받아서 성공적으로 소셜 로그인 기관에서 받은 인자값을 어떤 파일의 메서드들을 이용해 풀어낼건지 정도만 말해주는 미리 정의되어 있는 메서드인 것

 */