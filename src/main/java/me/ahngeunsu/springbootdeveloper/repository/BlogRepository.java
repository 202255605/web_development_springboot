package me.ahngeunsu.springbootdeveloper.repository;

import me.ahngeunsu.springbootdeveloper.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article, Long> {
}


// JpaRepository를 상속하는 인터페이스에 메서드 이름만 적어놓으면
//  알아서 다 처리(구현체 생성, 쿼리문 구현 등)해주는 좋은 ORM이다.
//  -> ORM 기술을 기반으로 구현이 되고 , 그래서 어쨋든 좋은 점은 findById 뭐 이런 구문을 작성을 하시면 -> 이 JPAREPOSITORY가 알아서 해석해가지고
// 쿼리 작성까지 다 하고 질문이나 결과값의 전송을 할때 사용하게 될 구현체까지 알아서 마련하고 진행을 하게 한다.

//메소드 이름은 findby(필드명), deleteby(필드명)처럼 메소드 명칭만 적어주면
//개발자는 SQL을 작성하지 않아도 쿼리문을 만들어준다.
// 
//이때 엔티티라는 클래스를 이용하는데 객체를 이용하여 매핑을 처리하는 것이다.
//어렵다면 엔티는 == 테이블 or 레코드라고 생각해 보자!
//출처: https://ccomccomhan.tistory.com/131 [[꼼꼼한 개발자] 꼼코더:티스토리]