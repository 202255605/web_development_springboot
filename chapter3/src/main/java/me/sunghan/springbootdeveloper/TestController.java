package me.sunghan.springbootdeveloper;

// 혹시나 기능의 구현을 했는데 적용이 되지 않을 경우
// 1. 서버를 껐다가 키세요
// 2. 그래도 안 되면 ctrl + s 눌러서 저장 한번 하시고
//3. intellij를 껐다가 킵니다
// 4. 서버를 재 실행 - 그러면 완료되는 경우가 대부분


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 로컬이라는 서버를 정지

    @GetMapping("/test")
    public String test(){
        return "Hello World!";
    }

}

