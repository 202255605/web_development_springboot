package me.sunghan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SpringBootDeveloperApplication {
    public static void main(String[] args) {
            SpringApplication.run(SpringBootDeveloperApplication.class, args);
        }
    }

/*
테스트 도구에 대한 설명들

스프링부트는 애플리케이션을 테스트 하기 위한 도구와 애너테이션을 제공
자바 언어를 위한 단위테스트 프레임워크
JUNIT : 자바 온오를 위하ㅓㄴ 테스트 프레임 워트  -> 단위테스트 : 작성한 코드가 의도대로 작동하는지 작은 단위로 검증함을 의미
보통 그 단위는 메서드 기준 AssertJ-> 검증문인 어써쎤을 작성하는데 사용되는 라이브러리

JUNIT을 사용하면 단위테스트의 결과가 직관적으로 나오는 편
테스트 방식을 구분할 수 있는 애너케이션 제공 , 등등

test/java 폴더에 JUNIT.java 파일을 만들어서 테스트 용 파일로 사용한다.

 */