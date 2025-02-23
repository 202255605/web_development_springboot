package me.ahngeunsu.springbootdeveloper.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/")
    public String home() {
        return "articleList";
    }
    // 기존 로그인 페이지
//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }

    // oauth 관련 로그인 페이지 -> 세션 로그인은 우리가 안 한기로 한지 오래요 , 이름 바꾸셨으면 바꾼 이름에 해당하는 .html 파일을 속으로 이동시켰다,.
    @GetMapping("/login")
    public String login() {
        return "oauthLogin";
    }
    //https://developers.google.com/identity/branding-guidelines 여기서 다운받을 수 있음.

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
