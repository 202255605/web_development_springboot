package me.ahngeunsu.springbootdeveloper.config.oauth;

//oauth2 인증과 관련한 요청 정보를 세션이 아닌 쿠키에 저장할 수 있도록 하는 설정 file
// 인증 '요청' 정보 와 인증 요청에 대한 '응답'정보를 어떻게 저장하고 어떻게 비교할 건가를 가르치지 않을까

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.ahngeunsu.springbootdeveloper.util.CookieUtil;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

public class OAuth2AuthorizationRequestBasedOnCookieRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"; // 쿠키의 이름을 oauth2_auth_request 로 설정함 , 이 안에는 이전에 oauth2 인증 요청 정보를 직렬화 해 저장해 놓은 곳이다.
    public final static int COOKIE_EXPIRE_SECONDS = 18000;

    // interface 에서의 implements 를 한 후 alt + insert 를 클릭면 이렇게 3개의 override를 해야하는 함수들이 저절로 그 틀이 작성된다.
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME); // webutils.getCookie(request)로 http request에서 추출한 "oauth2_auth_request" 라는 이름의 쿠키를 추출하여 밑에 줄로 넘겨준다.
        return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class);
    } //HTTP 요청에서 쿠키를 읽어와 OAuth2 인증 요청 정보를 복원합니다.

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,  // OAuth2AuthorizationRequest라는 객체는 인증정보를 나타내는 객체이고
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    } //OAuth2 인증 요청 정보를 쿠키에 저장합니다. 만약 인증 요청이 null이면 쿠키를 삭제합니다. 그렇지 않으면 인증 요청을 직렬화해서 쿠키에 저장합니다.

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {

        return this.loadAuthorizationRequest(request);
    }

    // 고유 메서드
    public void removeAuthorizationRequestCookies(HttpServletRequest request,
                                                  HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
/*
    config 패키지 -> OAuth2SuccessHandler.java 파일 생성
 */

// 인증 요청 정보는 현재 직렬화 된 채로 쿠키에 저장이 되어 있다 , 우리가 직점 http request 속 cookie를 뜯어서 인증 요청 정보를 loadAuthorizationRequest 메서드를 거치며 칼취하고 ,
// OAuth2AuthorizationRequest 라는 인증정보 저장용 클래스로 변환까지 한다

/*

OAuth2 소셜 로그인의 전체 흐름과 쿠키 인증 과정에 대한 설명!!!

최초 로그인 시도
->
    사용자가 "소셜 로그인" 버튼 클릭
    프론트엔드에서 백엔드의 인증 엔드포인트로 요청
    백엔드는 OAuth2 인증 요청 객체를 생성


인증 정보 저장 (여기서 위의 코드(OAuth2AuthorizationRequestBasedOnCookie)가 동작)
->
    생성된 OAuth2 인증 요청 객체를 쿠키에 저장
    saveAuthorizationRequest 메서드가 이 역할 수행
    쿠키 이름은 "oauth2_auth_request"로 저장
    이때 저장되는 정보: 클라이언트 ID, 리다이렉트 URI, 상태 토큰 등


소셜 로그인 제공자로 리다이렉트

사용자를 구글/네이버 등의 로그인 페이지로 리다이렉트


소셜 로그인 완료 후 콜백

사용자가 소셜 로그인 완료
제공자가 우리 서비스의 콜백 URI로 리다이렉트
이때 보내주신 코드의 loadAuthorizationRequest가 동작
저장해둔 쿠키에서 원래 인증 요청 정보를 복원

인증 완료 및 토큰 발급

복원된 정보를 바탕으로 인증 처리 완료
JWT 토큰 발급
removeAuthorizationRequest로 임시 저장된 쿠키 삭제



즉, 보내주신 코드는 이 전체 흐름에서 "인증 상태 보관소" 역할을 합니다. 특히:

인증 시작할 때 상태 저장 (saveAuthorizationRequest)
콜백 시점에 상태 복원 (loadAuthorizationRequest)
인증 완료 후 임시 상태 제거 (removeAuthorizationRequest)

이런 방식으로 stateless한 HTTP 환경에서 OAuth2 인증 과정의 상태를 안전하게 관리할 수 있게 됩니다

그리고 현재 이 파일에는 saveAuthorizationRequest 가 밑에 정의되어 있고 loadAuthorizationRequest 가 위에 정의되어 있는데

실제 이 인증정보를 다루는 논리적 순서로 보자면 saveAuthorizationRequest 를 위에 두고 loadAuthorizationRequest를 밑에 두는게 맞다

saveAuthorizationRequest 단계

소셜 로그인 요청 시점
인증 정보를 쿠키로 만들어 저장
CookieUtil.serialize()로 인증 정보를 직렬화
설정된 만료 시간(COOKIE_EXPIRE_SECONDS = 180초)과 함께 쿠키 생성
이 쿠키를 HTTP request에 포함


[소셜 로그인 진행]

사용자가 소셜 로그인 페이지에서 인증
인증 완료 후 우리 서비스로 리다이렉트


loadAuthorizationRequest 단계

소셜 로그인 완료 후 리다이렉트 되었을 때
WebUtils.getCookie()로 아까 저장한 쿠키를 찾음
CookieUtil.deserialize()로 쿠키 내용을 다시 객체로 변환
변환된 객체로 인증 정보 검증

이렇게 해서 로그인을 할려고 하는 클라이언트의 정보를 쿠키에 묶어서(saveAuthorizationRequest) 전달하면 해당 소셜로그인기관에서 쿠키의 내용을 뜯어 클라이언트의 정보를 확인한 후
해당 클라이언트에 대한 본인 인증을 해당 기관들의 로직대로 처리한다. 인증이 성공한다면 해당 클라이언트의 본인정보를 다시 쿠키에 담아 우리 서버로 전송
우리 서버에서는 해당 쿠키를 다시 뜯어서(loadAuthorizationRequest) 우리에게서 간 그 패킷에 대한 응답 패킷이 맞는지를 확인

이때 확인의 내용은
// 쿠키에서 꺼내서 확인하는 정보:
- 아까 저장해둔 상태 토큰이 맞는지 확인 (보안 체크)
- 원래 요청했던 리다이렉트 URI가 맞는지 확인
- 요청했던 인증 범위가 맞는지 확인

등등을 확인해 우리가 보낸 패킷이 맞음을 확인하고 그러면 해당 쿠키에서 소셜로그인 기관에서 보내준 정보를  OAuth2UserCustomService 에 정의된 대로 처리한다.
 */