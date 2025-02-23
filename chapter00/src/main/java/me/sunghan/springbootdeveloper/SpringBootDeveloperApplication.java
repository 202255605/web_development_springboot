package me.sunghan.springbootdeveloper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
모든 프로젝트는 main에 해당하는 클래스가 존재합니다 -> 실행용 클래스
이제 이 class를 main클래스로 사용할 예정입니다.
 */

@SpringBootApplication

public class SpringBootDeveloperApplication {
    public static void main(String[] args){
        SpringApplication.run(SpringBootDeveloperApplication.class , args);
    }  // 이 실행파일이 성공적으로 실행 되었다면 로컬호스트에서 시작되는 서버가 안정적으로 잘 실행 완료됨.

}

/*

처음으로 SpringBootDeveloperApplication 파일을 실행시키면 WhiteLaber Error page 가 뜹니다.
현재 요청에 해당하는 페이지가 존재하지 않기 떄문에 생겨난 문제이다 -> 하지만 스프링 애플리케이션은 실행됨!

그래서 error 페이지가 기분 나쁘니까 기본적으로 실행될때의 default페이지를 하나 설정하자

20241223 MON
1. intellij를 path.bin과 함께 설치했고 -> git설치, github연동
4. intellij 에 gradle , SpringBoot 프로젝트를 생성
4. PostMan을 설치함.
 */
