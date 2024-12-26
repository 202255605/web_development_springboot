package me.sunghan.springbootdeveloper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired
    MemberRepository memberRepository;  // 1. 빈 주입

    public List<Member> getAllMembers() {
        return memberRepository.findAll();  // 2. 멤버 목록 얻기  // 왜 findAll()인가에 주목하셔야합니다.
    }
    /*
        1. MemberRepository라는 빈을 주입 받은 후에,
        2. findAll() 메서드를 호출해서 멤버 테이블에 저장된 멤버 목록을 가져옵니다.
     */
}