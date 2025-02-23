package me.ahngeunsu.springbootdeveloper.config.oauth;

// 인증 요청 처리 완료 및 회원임이 모두 인증 되었아면 이제 진짜 어떻게 할건지에 대한 상세한 동작설명
// 사실 인증 요청을 통해 소셜로그인 기관에서는 우리의 개인 정보를 우리 사이트 쪽으로 전송해주는게 다고
// 우리는 그 정보를 바탕으로 우리 회원인지 여부를 알아보는 듯

// 이 코드의 주요  목적 :

// 이 코드의 주요 목적은 OAuth2 로그인 성공 후:
//
// 사용자 정보 확인 -> user 객체 가져오기
// JWT 토큰 발급 (액세스 토큰, 리프레시 토큰)
// 토큰 저장 및 쿠키 설정 (리프레시 토큰의 저장 & 더 이상 불 필요한 요소들의 쿠키에서의 삭제)
// 적절한 페이지로 리다이렉트(액세스 토큰과 함께)


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.config.jwt.TokenProvider;
import me.ahngeunsu.springbootdeveloper.domain.User;
import me.ahngeunsu.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.ahngeunsu.springbootdeveloper.dto.SaveRefreshTokenToDB;
import me.ahngeunsu.springbootdeveloper.repository.RefreshTokenRepository;
import me.ahngeunsu.springbootdeveloper.service.RefreshTokenService;
import me.ahngeunsu.springbootdeveloper.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
@Component  // import org.springframework.stereotype.Component;
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(2);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(10);
    public static final String REDIRECT_PATH = "/articles";

    private final TokenProvider tokenProvider; // 토큰생성기
    private final RefreshTokenRepository refreshTokenRepository;  //토큰저장소
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository; //인증요청저장소
    private final UserService userService; //사용자 서비스
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException , ServletException {
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("db 조작 (SaveOrUpdate) 완료 >< SuccessHandler 구동 시작");  // Java 클래스에서는 메소드나 생성자 외부에 직접적인 실행 구문을 작성할 수 없습니다.
        //import org.springframework.security.oauth2.core.user.OAuth2User;
        //OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal()

        System.out.println("Authentication 객체 확인: " + authentication); //
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();  // authentication.getPrincipal 하면 우리가 전송받는 값을 키-값 의 쌍들로 쫙 모아줌.

        System.out.println("사용자 속성들: " + attributes);
        System.out.println("사용자 email: " + attributes.get("email"));

        addAuthorizedManToCookie(request,response,(String) attributes.get("email"));

        if (authentication.getPrincipal() instanceof OAuth2User) {
            System.out.println("Principal이 OAuth2User 타입이다!");
        } else {
            System.out.println("Principal이 OAuth2User 타입이 아님!");
            return ; // 강제 함수 중지
        }

        User user = userService.findByEmail((String) oauth2User.getAttributes().get("email"));  // user객체로 받아옴 (사용자 정보 확인) -> 콘솔 창의 쿼리문이 여기서 나오는 쿼리 문이구만
        // 인증이 완료된 객체에 대해 그 객체가 소셜 로그인 기관에서 받아온 정보 중 email만을 추출해서 우리 사이트에 등록된 고객들의 정보가 저장되어 있는 User.java에 가서 Email을 바탕으로 그 고객의 entity를 받아온다.-> 객체 타입 : user

        System.out.println("Token 생성자 가동 준비 중 ...");

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION); // 인증이 완료된 객체에 대해 리프레시 토큰 발급

        System.out.println("refreshToken 발급 완료 : " + refreshToken);
        // 빨간거 뜹니다.
        saveRefreshToken(user.getEmail(), refreshToken);

        addRefreshTokenToCookie(request, response, refreshToken); // 여기가 refreshtoken을 client에게 전달하는 부분인 듯 -> accesstoken은 redirect url에 담아 전달하니까 

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);   // 인증이 완료된 객체에 대해 액세스 토큰 발급
                            // 빨간줄 뜹니다. -> 밑에 정의
        System.out.println("AccessToken 발급 완료 : " + accessToken);
        response.setHeader("Authorization", "Bearer " + accessToken); // 소셜로그인 기관에서 HTTP 응답에 토큰을 실어서 클라이언트(application)에게 보내는 것입니다. -> 밑에서 함수로 따로 구현 함.
        System.out.println("Response headers: " + response.getHeaderNames());
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        System.out.println("Authorization header value: " + response.getHeader("Authorization"));

        // 개발자 도구의 애플리케이션 -> 쿠키 에 들어가서 봤을때 refreshToken 이라는 이름의 쿠키 값이 잘 들어와 있다면 나의 액세스 토큰이 웹 서버 쪽에서 나에게 잘 들어온 것

        addAccessTokenToCookie(request,response,accessToken);

        /*
        브라우저나 모바일 앱 등 클라이언트는 이 응답을 받으면 Authorization 헤더에서 토큰을 추출합니다
        이 토큰을 localStorage, sessionStorage, 쿠키 등에 저장해둡니다
        이후 같은 서버로 요청을 보낼 때마다 저장해둔 토큰을 Authorization 헤더에 넣어서 요청을 보냅니다
         */
        
        String targetUrl = getTargetUrl(accessToken);

        System.out.println("redirect될 TargetUrl ---> " + targetUrl);

        clearAuthenticationAttributes(request, response);  // 더 이상 필요없는 인증 관련 세션 데이터를 정리 -> 이제서야 정리하는 건 앞에서 refreshtoken을 생성하고 전달해야 했기 때문에

        System.out.println("/articles로 redirect 준비 중");
        
        //getRedirectStrategy().sendRedirect(request, response, targetUrl);
        //SimpleUrlAuthenticationSuccessHandler(부모 클래스)에서 이미 구현되어 있는 메서드 -> 부모 클래스를 상속 받았으니 꼭 굳이 굳이 super을 쓸 필요는 없다 오버라이딩 하지도 않았으니까

        sendRedirectWithCustomHeader(request, response, targetUrl);

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

    }

    // OAuth2User oAuth2User = (OAuth2user) authentication.getPrincipal()  이 구문이 매우 어려울 수 있다.
    // 내부적으로 이렇게 동작합니다
    //Object principal = authentication.getPrincipal(); // Object 타입으로 반환 -> Authentication 클래스의 getPrincipal 은 object객체(형이 정확하게 정해지지 않은 객체를 반환)
    //OAuth2User oAuth2User = (OAuth2User) principal;  // OAuth2User 타입으로 변환 -> 이제 object라는 상위클래스의 객체를 하위 클래스인 OAuth2User 객체로 변환시켜야지
    // Authentication의 .principal 메서드의 역할은 ? -> authentication.getPrincipal()은 현재 인증된 사용자의 주요 정보를 가져옵니다 이 정보를 Object 객체로 반환하는 것

    private void saveRefreshToken(String user_email, String newRefreshToken) {

        // 리프레시 토큰을 서버에 저장하는 구문을 작성을 해주셔야 하는데.. 혹시 배포도 안 할거니까 리프레시 토큰까지 쓸 일이 없을 것 같아서 일부러 이 코드는 작성을 안 하신 건가??!
        // 내가 하지 머 -> 이 successHandler에서 생성한 리프레시 토큰에 맞는 dto를 새로 만들어서 RefreshToken에 넣어가지고 service.save 이렇게 하면 될 듯
        SaveRefreshTokenToDB saveRefreshTokenToDB = new SaveRefreshTokenToDB();
        saveRefreshTokenToDB.setUserId(user_email);
        saveRefreshTokenToDB.setRefreshToken(newRefreshToken);
        refreshTokenService.save(saveRefreshTokenToDB);
    }
    private void addRefreshTokenToCookie(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String refreshToken) {

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(1209600); // 14일 ( 초 단위 )
        // -> 우리가 맨 처음에 로그인을 탁 하면 /articles용 액세스 토큰과 , 내 브라우저 쿠키에 저장되는 리프레시토큰 이 두개를 받는다. 그리고 권한 인증이 필요한 모든 웹사이트 속 도메인에 들어갈때 쿠키 속 리프레시 토큰 기반의 새로운 액세스 토큰을 생성해서 들어가나.
        refreshCookie.setHttpOnly(true);
        /*
        HttpOnly가 true로 설정된 쿠키는 JavaScript를 통해 접근할 수 없습니다
        즉, document.cookie로 쿠키를 읽거나 수정할 수 없습니다 -> 우리는 스크립트 파일을 사용하긴 하지만 로그인 분야에 있어서는 뭐 안 쓰니까 이런 보안 조치 취해도 된다.
        오직 HTTP(S) 요청을 통해서만 서버로 전송됩니다
         */
        refreshCookie.setSecure(true);

        response.addCookie(refreshCookie);

    }

    private void addAccessTokenToCookie(HttpServletRequest request, HttpServletResponse response,String accessToken){
// 처음 SuccessHandler에서 인증이 완료되며 만든 리프레시 토큰과 액세스 토큰을 쿠키에다 저장해 놓으면 -> 매번 Filter에선 쿠키 속의 액세스 토큰의 유효성을 검사하고 괜찮으면 인증 부여 , 액세스 토큰 쿠키에 재 저장
        Cookie accessCookie = new Cookie("accessToken", accessToken);

        accessCookie.setPath("/");
        accessCookie.setMaxAge((int)ACCESS_TOKEN_DURATION.toSeconds());
        //  Duration.ofDays(1)은 시간을 나타내는 Duration 객체를 반환하는데, Cookie.setMaxAge()는 초 단위의 정수값(int)를 받습니다.-> 초단위 정수로 반환해야 함 -> ACCESS_TOKEN_DURATION.toSeconds()로 Duration을 초 단위로 변환
        //(int)로 형변환하여 Cookie.setMaxAge()가 요구하는 int 타입으로 변환
        accessCookie.setHttpOnly(true);
        /*
        HttpOnly가 true로 설정된 쿠키는 JavaScript를 통해 접근할 수 없습니다
        즉, document.cookie로 쿠키를 읽거나 수정할 수 없습니다 -> 우리는 스크립트 파일을 사용하긴 하지만 로그인 분야에 있어서는 뭐 안 쓰니까 이런 보안 조치 취해도 된다.
        오직 HTTP(S) 요청을 통해서만 서버로 전송됩니다
         */
        accessCookie.setSecure(true);

        response.addCookie(accessCookie);

    }


    private void clearAuthenticationAttributes(HttpServletRequest request,
                                               HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    //  public static final String REDIRECT_PATH = "/articles";
    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                //.queryParam("token", token)
                .build() // url 생성
                .toUriString();  // 생성한 url을 이용가능한 String 형으로 변환 , 즉 토큰 발급 하고 인증 필요한 곳의 주소 url까지 해서 한번에 다이렉트로 보내드릴께~
    }

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public void sendRedirectWithCustomHeader(HttpServletRequest request, HttpServletResponse response, String targetUrl) throws IOException {
        redirectStrategy.sendRedirect(request, response, targetUrl);
        //RedirectStrategy의 sendRedirect() 메소드는 내부적으로 HTTP 리다이렉트 응답을 보내는 과정에서 I/O 작업을 수행합니다.
        //I/O 작업 중에는 네트워크 문제나 다른 예외적인 상황이 발생할 수 있습니다.
        //이런 예외 상황을 처리할 수 있도록 Java에서는 IOException을 throw하도록 설계되어 있습니다.
    }

    private void addAuthorizedManToCookie(HttpServletRequest request, HttpServletResponse response, String Authorized_Man){
        Cookie Authorized_Man_Email = new Cookie("authorizedMan", Authorized_Man);

        Authorized_Man_Email.setPath("/");
        Authorized_Man_Email.setMaxAge((int)ACCESS_TOKEN_DURATION.toSeconds());
        //  Duration.ofDays(1)은 시간을 나타내는 Duration 객체를 반환하는데, Cookie.setMaxAge()는 초 단위의 정수값(int)를 받습니다.-> 초단위 정수로 반환해야 함 -> ACCESS_TOKEN_DURATION.toSeconds()로 Duration을 초 단위로 변환
        //(int)로 형변환하여 Cookie.setMaxAge()가 요구하는 int 타입으로 변환
        Authorized_Man_Email.setHttpOnly(true);
        /*
        HttpOnly가 true로 설정된 쿠키는 JavaScript를 통해 접근할 수 없습니다
        즉, document.cookie로 쿠키를 읽거나 수정할 수 없습니다 -> 우리는 스크립트 파일을 사용하긴 하지만 로그인 분야에 있어서는 뭐 안 쓰니까 이런 보안 조치 취해도 된다.
        오직 HTTP(S) 요청을 통해서만 서버로 전송됩니다
         */
        Authorized_Man_Email.setSecure(true);

        response.addCookie(Authorized_Man_Email);
    }


}


/*

getTargetUrl 에 대한 설명

UriComponentsBuilder 사용

UriComponentsBuilder.fromUriString(REDIRECT_PATH):

REDIRECT_PATH라는 상수에 정의된 기본 리다이렉트 경로로부터 URL을 생성합니다
예를 들어 REDIRECT_PATH가 "http://localhost:3000" 같은 프론트엔드 URL일 것입니다

쿼리 파라미터 추가
->
.queryParam("token", token):

URL에 ?token=액세스토큰값 형태로 쿼리 파라미터를 추가합니다
프론트엔드에서 이 토큰을 받아서 이후 인증에 사용합니다

URL 생성
->
.build(): URI 컴포넌트들을 조립합니다
.toUriString(): 최종 URL 문자열로 변환합니다

이때

REDIRECT_PATH가 "/articles"일 때 getTargetUrl 메서드의 실행 결과는:
/articles?token=eyJhbGciOiJ... (실제 액세스 토큰 값)
이런 형태가 됩니다.
이렇게 생성된 URL로 리다이렉트되면:

사용자는 "/articles" 경로로 이동하게 되고
이동된 페이지에서는 URL의 쿼리 파라미터로부터 토큰 값을 추출해서 사용할 수 있습니다
이 토큰은 이후 사용자가 우리 웹 사이트로 API 요청할 때 Authorization 헤더에 담아서 인증된 요청을 보내는 데 사용됩니다

즉, OAuth2 로그인 성공 → 우리 서버의 OAuth2SuccessHandler에 따라  "/articles?token=xxx" 로 리다이렉트 → client딴의 프론트엔드에서 토큰을 받아서 저장 → 이후 인증이 필요한 요청에 사용, 이런 플로우로 동작하게 됩니다.


*/


/*

    UserService.java로 넘어가도록 하겠습니다.

    지금 현재 email을 통한 유저 수정이 일어나고 있는 중입니다.
    OAuth를 위한 로직이 어느정도 구현됐으므로 작성한 글에 글쓴이를 추가하는 작업 -> 즉 어떤 글의 글쓴이가 본인이 맞을 경우에만 수정을 할 수 있는 기능이라고 생각하자

    domain -> Article.java로 가겠습니다
 */

/*
OAuth2User 객체에 대해 잘 모를 수 있는데

{
    "sub": "123456789",          // Google의 고유 식별자
    "name": "John Doe",          // 이름
    "email": "john@gmail.com",   // 이메일
    "picture": "https://...",    // 프로필 사진
    "email_verified": true       // 이메일 인증 여부
}

이런 식의 엔터티 객체라고 생각하면 편하다
그래서 OAuth2UserCustomService 를 보면 이런 구문이 있다 :

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");        // 다운캐스팅 예시
        String name = (String) attributes.get("name");

 */


/* 자 이제 진짜 마지막 !

clearAuthenticationAttributes(request, response);

private void clearAuthenticationAttributes(HttpServletRequest request,
                                               HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }


clearAuthenticationAttributes(request , response)
이 무슨 기능을 하는지도 모르겠고 , 또 이 정의에 있는 super.clearAuthenticationAttributes(request) ,
authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)

이 메서드 들도 무슨 일을 하는지 잘 모를거다

이 코드는 OAuth2 인증 과정에서 사용된 임시 세션 데이터를 정리하는 역할을 합니다.
쉽게 설명하면 이렇습니다:

OAuth2 로그인 과정에서는 잠시 '세션'을 사용합니다:

사용자가 소셜 로그인 버튼 클릭
구글/네이버 등으로 이동
다시 우리 서비스로 돌아오기


이 과정에서 다음과 같은 임시 데이터들이 세션에 저장됩니다:

인증 요청 상태
CSRF 토큰
리다이렉트 URI


로그인이 성공하고 JWT 토큰을 발급받은 후에는:

이 임시 데이터들이 더 이상 필요 없음
clearAuthenticationAttributes()로 이 데이터들을 깔끔히 정리
메모리 정리 + 보안상의 이유로 제거


clearAuthenticationAttributes 함수의 두 구문을 나눠서 설명해드리겠습니다:

super.clearAuthenticationAttributes(request);

이건 부모 클래스(SimpleUrlAuthenticationSuccessHandler)의 메서드를 호출하는 부분입니다.
세션에 저장된 기본적인 Spring Security 인증 관련 임시 데이터를 정리합니다.(정리하는거 자체가 기능인 메서드)
예: SPRING_SECURITY_LAST_EXCEPTION과 같은 인증 과정에서 발생한 예외 정보 등

authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

이건 우리가 직접 구현한 OAuth2 인증 과정에서 사용한 쿠키들을 정리하는 부분입니다.
OAuth2 인증 요청 정보를 쿠키에 저장했었는데, 이제 인증이 완료되었으니 그 쿠키들을 삭제합니다.
주로 OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME과 REDIRECT_URI_PARAM_COOKIE_NAME 같은 쿠키들이 삭제됩니다.


 */