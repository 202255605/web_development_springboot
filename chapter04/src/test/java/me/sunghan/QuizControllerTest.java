package me.sunghan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.AutoConfigureDataCassandra;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilder.webAppContextSetUp(context)
                .build();
    }

   /* ObjectMapper : JackSon 라이브러리에서 제공하는 클래스로 객체와 JSON간 변환을 처리

   즉 객체 직렬화

            */

    @DisplayName("quiz() : Post /quiz?code=2 이면 응답코드는 400 , 응답 본문은 Bad Request!을 반환한다")
    @Test
    public void getQuiz() throws Exception{

        // given
        final String url = "/quiz";

        //when
        final ResultActions result = mockMvc.perform(get(url)
                        .param("code" , "2"));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request!"));



    }

    @DisplayName("quiz() : Post /quiz?code=2 이면 응답코드는 403 , 응답 본문은 Forbidden!을 반환한다.")
    @Test
    public void postQuiz1() throws Exception{
        //given
        final String url = "/quiz";
        //when
        final  ResultActions result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new code 1)))
        //then
    }


}