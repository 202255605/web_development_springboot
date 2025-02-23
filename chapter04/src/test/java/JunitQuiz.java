import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// import static 의 경우 종종 보게 될텐데 정적 메서드에 해당하는 것을 가져올때 쓰입니다.

public class JunitQuiz {

    // String 으로 선언된 3개의 변수가 있는데 모두 일단 null이 아니고 , num1과 num2 가 같은 값이고
    // num3은 다른 값이 맞는지에 대한 판단을 하는 문제

    @Test
    public void junitTest1() {
        String name1 = "홍길동";
        String name2 = "홍길동";
        String name3 = "홍길동";

        assertThat(name1).isNotNull();
        assertThat(name2).isNotNull();
        assertThat(name3).isNotNull();

        assertThat(name1).isEqualTo(name2);
        assertThat(name1).isNotEqualTo(name3);

        int number1 =15 ;
        int number2 = 0 ;
        int number3 = -5 ;

        assertThat(number1).isGreaterThan(0);// isNotPositive, isNotGreaterThan 등 Not이 들어가 있는 구문들은 이상, 미만, 이하,초과 등의 개념을 가지므로 주의할 것
        assertThat(number2).isEqualTo(0); // isPositive(), isZero() , isNegative()를 썻어야 하는데
        assertThat(number3).isLessThan(0);

        assertThat(number1).isGreaterThan(number2);

        assertThat(number3).isLessThan(number2);



    }



}
