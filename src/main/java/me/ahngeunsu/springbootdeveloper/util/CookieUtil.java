package me.ahngeunsu.springbootdeveloper.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);   // import jakarta.servlet.http.Cookie;
        cookie.setPath("/");        //쿠키가 전체 웹사이트에서 유효하도록 설정 (쿠키의 유효 경로를 루트로 설정)
        cookie.setMaxAge(maxAge);   // 0이면 즉시 만료, 양수면 해당 초만큼 유지 -> 아마 쿠키의 유지 시간을 말하는 듯

        response.addCookie(cookie);
    }

    // 쿠키의 작동방식에 대한 깊은 연구의 결과가 밑에 작성되어 있다 스크롤로 내리자.

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {

        // String name : 삭제할 쿠키 이름

        Cookie[] cookies = request.getCookies(); // request에 cookies라는 인자타입으로 만들어진 리스트 같은 자료형이 원래 필드로 있었는 듯

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {    // 삭제할 쿠키 찾기
                cookie.setValue("");                // 값을 비움
                cookie.setPath("/");                // 경로 설정
                cookie.setMaxAge(0);                // 즉시 만료되도록 설정
                response.addCookie(cookie);         // 만료된 쿠키를 응답에 추가
            }
        }
    }

//    deleteCookie() : 쿠키 이름을 입력 받아서 쿠키를 삭제.
//            -> 실제로 삭제하는 방법은 없으므로 파라미터로 넘어온 키의 쿠키를
//    빈 값으로 바꾸고 만료 시간을 0으로 설정해서 쿠키가 재생성되자마자 만료처리를
//    함으로써 구성했습니다.


    // 객체를 직렬화 해서 쿠키의 값으로 변환
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));   // 결과값이 String으로 바뀐다.
    }

    // 쿠키를 역직렬화해서 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }
}
/*
    사용자 정보를 조회해 users 테이블에 사용자 정보가 있다면 리소스 서버(OAuth2관련) 에서
    제공해주는 이름을 업데이트 합니다.

    사용자의 정보가 없다면 새 사용자를 생성해서 DB에 저장할 수 있도록 하겠습니다

    먼저 , domain의 User.java를 수정하도록 하겠습니다.
*/


//cookie.getValue():
//
//쿠키에서 값을 가져옴
//이 값은 Base64로 인코딩된 직렬화 데이터
//
//Base64.getUrlDecoder().decode():
//
//URL-safe Base64로 인코딩된 문자열을 바이트 배열로 디코딩
//예: "SGVsbG8=" → byte[]
//
//SerializationUtils.deserialize():
//
//바이트 배열을 Java 객체로 역직렬화
//스프링의 SerializationUtils 클래스 사용
//
//cls.cast():
//
//역직렬화된 객체를 원하는 타입으로 캐스팅
//제네릭 타입 T로 안전하게 변환 -> 어떤 타입의 객체로 이 쿠키를 역직렬화 및 캐스팅 해야 할 지 모르기에 모든 가능성을 열어둔다 -> 탭플릿을 사용한다.
//


/*/////////////////////////////////////////////////////////////////////////////////////////////////*/

/*          jakarta.servlet 에서 실제 쿠키를 생성하고 이 쿠키를 클라이언트 단의 브라우저에 저장시키기까지의 일련의 과정

Cookie cookie = new Cookie(name, value);

이 구문이 실행되면:

Cookie 클래스의 생성자가 호출되면서 내부적으로 다음과 같은 검증과 처리가 일어납니다:

name과 value에 대한 유효성 검사 (허용되지 않는 문자나 공백 체크)
name과 value를 RFC 6265 표준에 맞게 인코딩
쿠키의 기본 속성들 초기화 (version, path 등)
->              cookie.setValue("");                // 값을 비움
                cookie.setPath("/");                // 경로 설정
                cookie.setMaxAge(0);

이런 구문들을 써 왔던 이유가 쿠키 클래스의 필드 값들에 대한 초기화를 하기 위해서 였던 것

HTTP 응답 헤더 생성:

response.addCookie(cookie);
이 메서드가 호출되면( 이 소스코드 파일의 위에 보면 딱 있음)

ServletResponse 구현체는 "Set-Cookie" HTTP 헤더를 생성

쿠키 정보를 다음과 같은 형식으로 변환:
Set-Cookie: name=value; Domain=domain; Path=path; Expires=date; Secure; HttpOnly

브라우저로의 전송:

생성된 HTTP 응답 헤더는 클라이언트로 전송됨
브라우저는 이 헤더를 해석하여 로컬에 쿠키 저장

// 마지막으로 jakarta.servlet.http.cookie 클래스의 원본을 확인해보자

public class Cookie {
    private String name;
    private String value;

    public Cookie(String name, String value) {
        // 1. 이름 유효성 검사
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid cookie name");
        }

        // 2. 값 인코딩
        this.name = name;
        this.value = encode(value);

        // 3. 기본 속성 설정
        this.path = "/";
        this.secure = false;
        this.httpOnly = false;
    }

    private String encode(String value) {
        // URL 인코딩 수행
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
*/
// 자자 아주 귀한 개념들이 많이 나온다 하나씩 알아보자

/*

1. SerializationUtils.serialize(객체) + Serialization.deserialize(serialized_value)

의 개념은 이름은 직렬화 / 역직렬화 이지만 우리가 이때까지 써왔던 java객체를 http로 보내기 위해 ObjectMapper을 이용해 직렬화 , 역직렬화 해 왔던 것과는 다른 개념이다.

바이트 스트림 직렬화:

Java의 내부 직렬화 메커니즘을 사용
객체를 바이너리 형태(0과 1의 시퀀스)로 변환
Java 전용이며 다른 플랫폼과 호환되지 않을 수 있음

// 바이트 스트림 직렬화 예시
User user = new User("John", 25);
byte[] bytes = SerializationUtils.serialize(user);
// 결과: [바이너리 데이터] - 사람이 읽을 수 없는 형태

JSON 직렬화:

텍스트 기반의 데이터 포맷
사람이 읽을 수 있는 형태
여러 플랫폼과 언어에서 사용 가능

// JSON 직렬화 예시
User user = new User("John", 25);
String json = objectMapper.writeValueAsString(user);
// 결과: {"name":"John","age":25} - 사람이 읽을 수 있는 형태

귀하의 코드는 Java의 바이트 스트림 직렬화를 사용하고 있으며, 이는 JSON 변환과는 다른 메커니즘입니다.



2. 제네릭의 활용!

Class<T>와 cls.cast():

Class<T>: 제네릭을 사용한 클래스 타입 정보를 나타냅니다. T는 타입 파라미터로, 어떤 타입이든 될 수 있습니다.
cls.cast(): 객체를 지정된 타입으로 안전하게 형변환합니다.

예시로 보면 이해가 쉽습니다:

// 사용 예시
Cookie cookie = new Cookie("auth", "encoded_value");
User user = deserialize(cookie, User.class);  // User.class가 Class<T>에 해당 -> 어떤 타입의 클래스던 들어갈 수가 있다

내가 정의한 클래스도 괜찮고 String 과 같은 java에서 기본으로 제공해주는 class 타입들도 모두 상관없다.

// 내부적으로는 이렇게 동작
Object deserialized = SerializationUtils.deserialize(...);
User user = User.class.cast(deserialized);  // 역직렬화된 객체를 User 타입으로 캐스팅

즉 public static <T> T deserialize(encoded_value , class<T> cls) 이 함수의 내부 동작과정을 보자면 아래와 같다.

쿠키의 값을 Base64 디코딩 -> 암호화 해제
디코딩된 바이트 스트림을 객체로 역직렬화 -> 사람이 읽을 수 있는 형태로의 변환
역직렬화된 객체를 원하는 타입(T)으로 안전하게 형변환

이 코드는 주로 쿠키에 저장된 객체를 다시 원래의 자바 객체로 복원할 때 사용됩니다.

 */
