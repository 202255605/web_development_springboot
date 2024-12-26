package me.sunghan.springbootdeveloper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
모든 프로젝트는 main에 해당하는 클래스가 존재합니다 -> 실행용 클래스
이제 이 class를 main클래스로 사용할 예정입니다. 자바를 위한 클래스지 스프링 부트를 위한 클래스는 아니라는 것

 new project의 생성시 주의점 :
 1. build system -> gradle 설정
 2. DLS -> groovy 설정
 3. name = artifactId
 4.  build.gradle 을 복붙 하고 난 후에는 꼭 sync 해줘야 한다.-> sync를 꼬꼭 눌러줘야 한다 안 하면 @SpringBootApplication애너테이션에 빨간줄 뜸
 5. resources 내에 static 내에 index.html 이라고 하는데 , 해당 폴더명의 경우 대부분의 개발자들이 합의한 상태
6. 앞으로의 수업 시간에는 new project생성시에 이렇게 자세히 풀이할 일은 자주 있지 않을 예쩡이니 , 꼭 익혀놓아야 한다.
-> github를 본ㄷ고 되는 부분이 아니니만큼 꼭 신경쓰시길

 */


/*

    1. spring -> Enterprise Application 대규모의 복잡한 데이터를 관리 , 많은 사용자의 요청이 있어서 여러가지 신경쓸 것들을
    한번에 모아서 처리해 줄 수 있는 그런 툴이 필요해서 만들어진 Spring

    2. SpringBoot -> Spring은 설정이 너무 복잡해 이 단점을 해결하기 위한 툴이 스프링 부트

     1> 웹 애플리케이션이 내장되어 있어서 따로 설치하지 않아도 독립적으로 실행이 가능
     2> 빌드 구성을 단순화 하는 스프링 부트 스타터를 제공
     3> XML 을 설정을 하지 않고 자바 코드로만 작성이 가능
     4> JAR을 이용해서 자바 옵션만으로 배포가 가능
     5> 애플리케이션 모니터링 및 관리도구인 스프링 액츄에이터 제공 -> ci/cd 즉 지속적인 관리 및 감시 의 기능을 제공한다는 것


    즉 스프링 부트라는 것은 기본적으로 스프링에 속해있는 도구라는 것

    차이점 : 스프링은 애플리케이션 개발에 필요한 환경을 수동으로 구성하고 정의 하지만 스프링부트는 스프링 코어와 스프링 mvc의 모든 기능을 자동으로로드
    그러므로 수동으로 개발환경을 구성할 필요가 없음

    내장 was의 유무에서도 차이가 나는데 -> 스프링 애플리케이션은 일반적으로 톰캣과 같은 WAS에서 배포함 하지만 스프링 부트는 자체적으로 WAS
    jar파일만 만들면 별도로 WAS 설정을 하지 않아도 애플리케이션을 실행할 수 있음.


    2. 스프링 컨셉
    1. 제어의 역전 (IOC):
    inversion of control 다른 객체를 직접 생성하거나 제어하는 것이 아니라 외부에서 관리하는 객체를 가져와 사용하는 것을 의미함.
    2.클래스 A에서 클래스 B의 객체를 생성한다고 가정했을때의 자바 코드
    public class A {
        B b = new B();
    } 이게 일반적인 클래스 선언의 코드인데

    위와는 다르게 틀래스 B의 객페를 직접 생성하는 것이 아니라 어딘가에서 받아와 사용을 함( 즉 new를 쓰지 않게된다는 것이지), 실제로
    스프링은 스프링 컨테이너에서 객체를 관리 , 제공하는 역할을 함.

    public class A {
        private B b ;
    }  이렇게 된다.

    2) 의존성의 주입 -> Dependency Injection : 어떤 클래스가 다른 클래스에 의존한다는 의미

    @Autowired 애너테이션이 중요 -> 스프링 컨테이너에 있는 bean을 주입하는 역할
    Bean :  스프링 컨테이너가 관리하는 객채-> 추후 한번 더 수업할 예쩡

    객체를 주입 받는 모습 의 예제

    public class A {
     @ Autowired
      B b ;
    }

    3) 빈과 스프링 컨테이너

    1. 스프링 컨테이너 : 빈이 생성되고 소멸되기 까지의 생명주기를 관리함.
    또한 @autowired 와 같은 애너테이션을 사용해 빈을 주입받을 수 있게 DI를 지원함.

    2. 빈 : 스프링 컨테이너가 생성하고 관리하는 객체 -> 이상의 코드들에서 B가 빈에 해당
    스프링은 빈을 스프링 컨테이너에 등록하기 위해 XML 파일 설정 , 애너테이션 추가 등의 방법을 제공하는데 이섯이 의미하는 바는

    1. 빈을 등록하는 방법은 여러가지인다 근데 우리가 수업에서 사용할 방법은 애너테이션이다.

    클래스를 빈으로 등록하는 방법의 예쩨
    @component
    public class MyBean{}

    이상과 같이 @Component 라는 애너테이션을 붙이면 MyBean 클래스가 빈으로 등록됨 이후 스프링 컨테이너에서 이 클래스를 관리하고 빈의 이름은
    첫문자를 소문자로 바꾸어서 관리함 즉 클래스 MyBean의 빈 이름은 myBean 이 뒴

    일반적인 스프링이 제공해주는 객체로 받아들이시면 됩니다.


    4> 관점 지향 프로그래밍 -> AOP Aspect-oriented Programming :프로그래밍에 대한 관심을 핵심괁ㅁ / 부가 관점으로 나누어 모듈화함을 의미

    ex> 계좌이


    5. 이식 가능한 서비스 추상화 : PSA

    portable Service Abstraction : 스프링에서 제공하는 다양한 기술들을 '추상화' 해 개발자가 쉽게 사용하는 '인터페이스'

    3.스프링 부트 3 둘러보기

    첫번째 스프링부트 3 예제 만들기

    01단계 -> springbootdeveloper

    // 스프링부트 스타터 살펴보기

    스프링부트 스타터는 의존성이 모여있는 그룹에 해당함
    스타터를 사용할 경우 필요한 기능을 간편하게 설정이 가능
    스타터의 명명규칙 -> spring-boot-starter-{작업유형}

    스타터의 예시
    spring-boot-starter-web
    spring-boot-starter-test
    spring-boot-starter-validation
    spring-boot-starter-actuator
    spring-boot-starter-jap

 */

@SpringBootApplication

public class SpringBootDeveloperApplication {

    public static void main(String[] args){
        SpringApplication.run(SpringBootDeveloperApplication.class , args);

    }

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
