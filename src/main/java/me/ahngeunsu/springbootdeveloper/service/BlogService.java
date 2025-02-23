package me.ahngeunsu.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.Article;
import me.ahngeunsu.springbootdeveloper.dto.AddArticleRequest;
import me.ahngeunsu.springbootdeveloper.dto.UpdateArticleRequest;
import me.ahngeunsu.springbootdeveloper.repository.BlogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {

    private final BlogRepository blogRepository;  // Article.java와 직접 연관되는 Service 계층의 파일인 BlogService.java도 수정 들어가야지

    public Article save(AddArticleRequest request,
                        String userName) {
        return blogRepository.save(request.toEntity(userName));
    }
    // 인자로 들어온 AddArticleRequest 클래스의 request객체가 이미 생성당시 필드로 title, content를 가지고 있어서 -> .toEntity를 통해 builder 패턴을 실행할때 추가적인 요소로써 author 만 있으면 된다.

    public List<Article> findAll() {
        return blogRepository.findAll();
    }


    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));   // 람다식
    }

//    public void delete(long id) {
//        blogRepository.deleteById(id);
//    }
//
//    @Transactional
//    public Article update(long id, UpdateArticleRequest request) {
//        Article article = blogRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));
//
//        article.update(request.getTitle(), request.getContent());
//
//        return article;
//    }

    public void delete(long id)   {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }
    /*
    모든 작업이 성공적으로 완료되어야 최종 커밋
    하나라도 실패하면 이전 상태로 롤백
    데이터의 일관성을 보장
    동시성 문제 처리 지원
     */

    private void authorizeArticleAuthor(Article article) {
        System.out.println("작성자 검증");
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("현재 접속자 " + userName );
        if (!article.getAuthor().equals(userName)) {
            System.out.println("수정 권한 없음");
            throw new IllegalArgumentException("not authorized");
            //어떤 User가 어떤 동작을 하려고 할때 해당 User가 할 수 있는 동작인지의 여부를 판단하는 메서드 생각보다 졸라 간단하네 그렇지
            //권한을 부여받은 채로 ArticleList.html 여기에 들어왔고 여기 바로 직전에는 localhost:8080/articles 로 들어왔었겠지!
            //결국 흐름이 : 회원 인증 -> /articles -> ArticleList.html -> Token.js/Article.js -> 자신이 html 파일에서 선택한 버튼에 따라 Article.js에서 이어줌 (리다이렉트로!)
        }
    }

}
