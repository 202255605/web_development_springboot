package springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ahngeunsu.springbootdeveloper.config.jwt.JwtProperties;
import me.ahngeunsu.springbootdeveloper.domain.RefreshToken;
import me.ahngeunsu.springbootdeveloper.domain.User;
import me.ahngeunsu.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.ahngeunsu.springbootdeveloper.repository.RefreshTokenRepository;
import me.ahngeunsu.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import springbootdeveloper.config.jwt.JwtFactory;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    Given
        - 테스트 유저를 생성, jjwt 라이브러리 이용해서 리프레시 토큰을 만들어
        DB에 저장, 토큰 생성 API의 요청 본문에 토큰을 포함하여 요청 객체를 생성

    When
        - 토큰 추가 API에 요청을 보냅니다. 이때 요청 타입은 JSON이며,
        given 절에서 미리 만들어둔 객체를 요청 본문으로 함께 보냅니다.

    Then
        - 응답 코드가 201 Created인지 확인하고, 응답으로 온 액세스 토큰이
        비어있지 않은지 확인
 */



@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        userRepository.deleteAll();
    }

    @DisplayName("createNewAccessToken() : 새로운 액세스 토큰을 발급")
    @Test
    public void createNewAccessToken() throws Exception {
        // given
        final String url = "/api/token";

        User testUser = userRepository.save(User.builder() // testUser니까 임의로 설정
                .email("user@gmail.com")
                .password("text")
                .build());

        String refreshToken = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build().createToken(jwtProperties);

        refreshTokenRepository.save(RefreshToken.builder().user_id(testUser.getEmail()).refreshToken(refreshToken).build());

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        //import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
        //import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        //import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
    }


}
/*
    chapter09
        토큰 기반 인증의 특징
        그 중에서 JWT을 적용하고, 토큰 제공자(TokenProvider)를 만들었습니다.

        해당 인증 방법은 대중적으로 사용되는데다가
        JWT이 좀 현재 상황에서는 인기가 있는 편입니다.

        Token : 클라이언트를 구분하는 데 사용하는 유일한 값
            서버에서 생성한 뒤에 클라이언트에게 제공한 뒤
            클라이언트는 서버에 요청할 때마다 요청 내용과 함께 토큰 전송,
            서버가 하는 일은 토큰이 유효한 사용자인지 확인하는 것
            -> 세션 로그인 방식(form login)과의 차이

        JWT : 토큰 기반 인증 중 현재 인기 있는 방식 JSON 형식으로
            클라이언트의 정보를 저장.
            구성 :
                헤더 / 내용 / 서명 구조로 이루어져 있음.
                헤더 - 토큰의 타입과 해싱 알고리즘(JWT / HS256을 적용)
                내용 - 토큰에 담을 정보
                서명 - 토큰이 조작되었거나 변경되지 않았음을 확인하는 용도
        Refresh Token : 액세스 토큰과 별개의 토큰. 액세스 토큰이 만료되었을 때
            새로운 액세스 토큰을 발급받는 용도
            (CreateAccessTokenRequest로 RefreshToken을 포함해서 요청했고, 그
            결과값으로 AccessToken을 반환받는 구조로 구현했었습니다.)
        Filter : 실제로 요청되기 전과 후에 URL 패턴에 맞는 모든 요청을 처리하는 기능 제공
            - 세션 로그인 기준으로는 API 하나하나 마다 요청 전후에 검증이 필요했었습니다.(차이점)

        Security Context : 인증 객체가 저장되는 보관소, 인증 정보가 필요할 때마다
            인증 객체를 꺼내어 사용하도록 제공되는 클래스.
            시큐리티 컨텍스트 객체를 저장하는 객체 -> Security Context Holder였습니다.
            여기서 get(필요한 값)의 형식으로 필요한 인증 점보를 추출해 냅니다.
 */