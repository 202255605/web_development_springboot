package me.ahngeunsu.springbootdeveloper.config;

// 클라이언트 → OAuth2 로그인 요청
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.config.jwt.TokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer "; // 한칸 띄어줘야 이제부터 매번 요청 속 header 칸에 같이 포함되서 올 토큰을 잘 분리해서 잘 확인하지


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

//        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION); // SuccessHandler의 response.setHeader("Authorization" + "Bearer" + accessToken) 구문을 통해 헤더에 넣은 액세스 토큰 추출하는 중
//        System.out.println("Authorization 헤더 값: " + authorizationHeader); // 헤더 자체가 없는 경우를 체크하기 위해

        String token = extractTokenFromCookie(request);
        // SuccessHandler 속 액세스토큰 추출
        //String token = request.getParameter("token");  // URL 쿼리 파라미터에서 토큰 추출 -> 이렇게 하면 모든 Controller의 도메인들에 토큰 다 붙여야 함 -> 불가능
        System.out.println("Filter 에서 추출해낸 응답 http 쿠키 속 액세스 token : " + token);

        if (token != null && tokenProvider.validToken(token)) {
            System.out.println("유효한 토큰입니다 -> 접속 엔진 구동 , 인가 정보 ContextHolder에 저장");
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 한번 유효한 토큰이라고 filter에서 판명나면 더 이상 쿠키 값에 토큰 안 넣어 다녀도 되겠네 -> SecurityContextHolder에 이미 인증값이 들어 있으니까
            filterChain.doFilter(request, response);
        } else {
            // 유효하지 않은 토큰이 되면 -> authentication을 받지 못한 채 다음 filter 단계로 넘어감 -> Security Config 파일을 보자면
            if(token == null){
                System.out.println("\n token 값이 null 입니다 -> SuccessHandler 에서 쿠키에 저장해온 token 값이 없습니다 ");
            }else{
                System.out.println(("token 값이 null은 아니지만 유효한 토큰은 아닙니다."));
            }
            filterChain.doFilter(request, response);
        }

    }
    /*
    여기서 문제점이 보입니다:

토큰 전달 방식의 불일치:


SuccessHandler에서는 토큰을 URL 쿼리 파라미터로 전달: /articles?token=xxx
Filter에서는 토큰을 Authorization 헤더에서 찾으려고 시도: request.getHeader(HEADER_AUTHORIZATION)

이렇게 되면:

SuccessHandler가 /articles?token=xxx 형태로 리다이렉트
Filter가 실행되면서 Authorization 헤더를 찾지만 찾을 수 없음
결과적으로 토큰 검증 실패

     */
    // 리프레시 토큰 기반 액세스 토큰 발급 로직
//    private String getAccessToken(String authorizationHeader) {
//        if(authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
//            return authorizationHeader.substring(TOKEN_PREFIX.length());
//        }
//        return null;
//    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}


/*
filterChain.doFilter(request, response); 의 기능을 모를 수 있다 하지만 매우 중요한 내용이다.

ilterChain.doFilter(request, response)는 Spring Security의 필터 체인에서 매우 중요한 역할을 하는 코드입니다.
이 코드의 의미를 이해하기 위해서는 Spring Security의 필터 체인 동작 방식을 알아야 합니다:

필터 체인의 개념:

Spring Security는 여러 필터들이 체인처럼 연결되어 있는 구조를 가집니다.
각 필터는 특정한 보안 기능을 담당하며, 순차적으로 실행됩니다.

CopyRequest → Filter1 → Filter2 → Filter3 → ... → 실제 요청 처리

filterChain.doFilter()의 역할:
if(tokenProvider.validToken(token)) {
    Authentication authentication = tokenProvider.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
filterChain.doFilter(request, response);  // 여기!

현재 필터에서 자신의 작업을 마친 후, 다음 필터로 요청을 전달하는 역할을 합니다.
이 라인이 없다면 필터 체인이 중단되어 다음 필터로 진행되지 않습니다.
만약 이 코드가 호출되지 않으면 실제 요청을 처리하는 컨트롤러까지 도달할 수 없습니다.

실행 흐름 예시:
1. TokenAuthenticationFilter
   - 토큰 확인
   - 인증 정보 설정
   - filterChain.doFilter() 호출
2. 다음 필터
   - 자신의 로직 수행
   - filterChain.doFilter() 호출
3. 그 다음 필터...

즉, filterChain.doFilter(request, response)는 "내 할 일은 다 했으니, 다음 필터도 자신의 일을 하세요"라고 전달하는 바톤 역할을 한다고 볼 수 있습니다.

 */