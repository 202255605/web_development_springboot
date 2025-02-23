package me.ahngeunsu.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "author", nullable = false) // 해당 글을 쓴 사람 즉 글쓴이
    private String author;

    @Builder
    public Article(String author, String title, String content) {  // 어떤 글이 생성될때 무조건 누가 생성했는지 즉 author이 무조건 포함되게 제작
        this.author = author;
        this.title = title;
        this.content = content;
    }   // - builder 파트를 수정했으므로 dto도 수정돼야 합니다 -> AddArticleRequest.java의 toEntity() 수정해야 함.

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
