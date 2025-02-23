package me.ahngeunsu.springbootdeveloper.controller;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import me.ahngeunsu.springbootdeveloper.domain.Article;
import me.ahngeunsu.springbootdeveloper.dto.ArticleListViewResponse;
import me.ahngeunsu.springbootdeveloper.dto.ArticleViewResponse;
import me.ahngeunsu.springbootdeveloper.service.BlogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;

    private String getAuthorizedManName(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //이랬더니 더이상 null은 아님 , 즉 잘 받아오는 방법이다 근데 이랬더니 이제 authentication이 OAuth2User타입이 아닌 String타입으로 옴 -> 그래서 오류 발생
//authentication 이라고 뜨면 인증정보가 유실된 상황일 확률이 높다
        System.out.println("authentication의 Actual type: " + authentication.getClass().getName());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        System.out.println("현재 인증 객체: " + SecurityContextHolder.getContext().getAuthentication()); // authentication에서 제대로된 principal값이 안 불러와지고 있다.

        if ( oAuth2User != null ) {
            System.out.println("OAuth2User 속성들: " + oAuth2User.getAttributes()); // 하나밖에 안 뜨는 이유 -> application.yml 때문에

            Map<String, Object> attributes = oAuth2User.getAttributes();

            System.out.println("Map 으로 캐스팅 한 기념으로 한번 더! 사용자 속성들: " + attributes);

            /*
            Map<String, Object>에서:

    key인 "email"은 String 타입입니다
    value인 "sunghan1228@naver.com"은 실제로는 String 객체이지만, Map에서는 Object 타입으로 취급됩니다

        따라서 이 값을 실제로 String으로 사용하려면 다음과 같이 형변환(casting)이 필요합니다:
             */
            String email = (String) attributes.get("email");

            String name = email.substring(0, email.indexOf("@"));

            System.out.println("사용자 이름: " + name);

            model.addAttribute("userNickname", name);

            return name;
        } else {
            System.out.println("OAuth2User가 null입니다");
            return null ;
        }
    }

    @GetMapping("/articles")
    public String getArticles(Model model) {

        //지금 Authentication을 파라미터로 직접 받고 있는데 이렇게 하면 Spring MVC가 자동으로 Authentication 객체를 주입해야 하는데, SecurityContext가 제대로 유지되지 않아서 null이 주입되는 것 같습니다.
        //그래서 authentication을 전용으로 소유하고 관리하는 객체인 SecurityContextHolder이 있나보다

        System.out.println("--------------------------------- /articles - Controller 진입 ----------------------------------------------------------------------------------------------");

        getAuthorizedManName(model);

        List<ArticleListViewResponse> articles = blogService.findAll().stream()
                .map(ArticleListViewResponse::new).toList(); 
        // Article.java의 column의 수와 종류가 바뀌었는데 이 Article.java 와 매핑되는 ArticleViewResponse 도 바뀌어야지 그래 아니면 매핑 안 되서 애러난다.

        model.addAttribute("articles", articles);
        // 후에 타임리프 등에서 정보를 참조할 때 사용할 이름이다
        // <div th:each="article : ${articles}">
        //    <h2 th:text="${article.title}">제목</h2>
        //    <p th:text="${article.content}">내용</p>
        //</div>
        return "articleList";
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable Long id, Model model) {
        getAuthorizedManName(model); // 이건 flont web page의 sunghan1228's blog 이 문구 위해 필요한 것 (! 수정 , 삭제 권한 체크를 위한 것)
        Article article = blogService.findById(id);
        model.addAttribute("article", new ArticleViewResponse(article));
        return "article";
    }

    @GetMapping("/new-article") // article.js에서 인자가 없는 상태에서 넘어오면 -> 새 글 , 인자가 있는 상태에서 넘어오면 -> 수정 글
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        getAuthorizedManName(model); // 표지에 나타내기 위해 필요해 -> 지우면 안 돼
        if(id == null) {
            ArticleViewResponse articleViewResponse = new ArticleViewResponse();
            articleViewResponse.setAuthor(getAuthorizedManName(model));
            model.addAttribute("article", articleViewResponse);
        } else {
            Article article = blogService.findById(id);
            System.out.println("\n 수정하려고 하는 객체 안에 담긴 field 들을 출력 \n");
            System.out.println("id: " + article.getId());
            System.out.println("title: " + article.getTitle());
            System.out.println("content: " + article.getContent());
            System.out.println("author: " + article.getAuthor()); // 오 객체 필드들 뽑아 내는 거 이거 좋네 이러라고 getter 설정 하는 거구나
            model.addAttribute("article", new ArticleViewResponse(article));
            System.out.println("일단 수정하려는 객체의 원본 전송 까지는 완료");
        }
        return "newArticle";
    }


}
