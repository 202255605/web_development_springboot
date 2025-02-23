import org.junit.jupiter.api.*; // all이라는 뜻

public class JunitCycleTest {

    @BeforeAll
    static void beforeAll(){
        System.out.println("@beforeAll");
    }
    @BeforeEach
    public void beforeEach(){
        System.out.println("@beforeAll");
    }

    @Test
    public void test1(){
        System.out.println("@test1");
    }

    public void test2(){
        System.out.println("@test2");
    }

    public void test3(){
        System.out.println("@test3");
    }

    /*
    @beforeAll
    전체 테스트를 시작하기 전에 처음으로 한 번만 실행
    데이터베이스에 연결해야 하거나 테스트 환경을 초기화할 때 사용
    실행 주기에서 한번만 호출 돼야 하기 떄문에 메서드를 static으로 선언(정적 메서드)

    isEqualTo(A) : A와 같은지 검증
    isNotEqualTo(A) : A와 다른지 검증
    contains(A) : A를 포함하는지의 검증
    doesNotContain(A)
    startsWith

    테스트 코드 작성 연습 문제 풀어보기
    -> test/java 폴더에 JunitQuiz에서 만나요



    */


}
