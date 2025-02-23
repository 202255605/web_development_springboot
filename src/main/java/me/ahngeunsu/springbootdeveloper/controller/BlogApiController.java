package me.ahngeunsu.springbootdeveloper.controller;

// article.html(Modify-button/Delete-button) -> article.js -> BlogApiController

import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.Article;
import me.ahngeunsu.springbootdeveloper.dto.AddArticleRequest;
import me.ahngeunsu.springbootdeveloper.dto.ArticleResponse;
import me.ahngeunsu.springbootdeveloper.dto.UpdateArticleRequest;
import me.ahngeunsu.springbootdeveloper.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class BlogApiController {

    private final BlogService blogService;

    @PostMapping("/api/articles") // 새로 작성
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleRequest request
 // principal은 java 내부에서 제공하는 API로 부터 가져오는 인증이 완료된 사용자 객체타입이고 그런 타입인 Principal의 인스턴스 principal 이 함께 쓰인다 , 필드에 name이 있나보네(lombok getName)
                                            , Principal principal) {
        Article savedArticle = blogService.save(request, principal.getName()); // request의 인자 즉 post 요청 시 body에 있는 내용 : title , content 근데 body에 인증완료된 사용자 객체가 들어가 있어야 한다는 말

        return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();

        return ResponseEntity.ok().body(articles);
    }

    @GetMapping("/api/articles/{id}")

    public ResponseEntity<ArticleResponse> findArticle(@PathVariable long id) {
        Article article = blogService.findById(id);

        return ResponseEntity.ok()
                .body(new ArticleResponse(article));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable long id) {
        blogService.delete(id);

        return ResponseEntity.ok()
                .build();
    }

    @PutMapping("/api/articles/{id}") // 수정
    public ResponseEntity<Article> updateArticle(@PathVariable long id, @RequestBody UpdateArticleRequest request) { // 새로운 내용
        Article updateArticle = blogService.update(id, request);

        return ResponseEntity.ok().body(updateArticle);
    }
}








