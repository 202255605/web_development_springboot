# 웹 게시판 개발 프로젝트

> 개발 기간: 2024.01.18 - 2024.02.14

Spring Boot 기반의 풀스택 게시판 시스템을 설계하고 개발한 프로젝트입니다. 사용자 인증 및 게시글 관리 기능을 포함하며, RESTful API와 보안 시스템을 구현했습니다.

## 📚 기술 스택

### Backend
- **Spring Boot 3.2.x**: 웹 애플리케이션 프레임워크
- **Java 17**: 주요 개발 언어
- **Spring Security**: 인증 및 권한 관리
- **Spring Data JPA**: 데이터 접근 계층
- **JWT (JSON Web Token)**: 사용자 인증 토큰 관리
- **OAuth2**: 소셜 로그인 구현
- **H2 Database**: 개발 환경 데이터베이스
- **Hibernate**: ORM(Object-Relational Mapping)
- **Maven/Gradle**: 의존성 관리 도구

### Frontend (선택적)
- **Thymeleaf**: 서버 사이드 템플릿 엔진
- **HTML/CSS/JavaScript**: 기본 웹 기술
- **Bootstrap**: 반응형 디자인 프레임워크

## 🚀 주요 기능

### 회원 관리 시스템
- JWT 토큰 기반의 사용자 인증 시스템 구축
- OAuth2를 활용한 소셜 로그인 구현 (Google, Naver, Kakao 등)
- Spring Security를 활용한 역할 기반 접근 제어
- 사용자 프로필 관리

### 게시판 핵심 기능
- 게시글 CRUD 및 페이징 처리 구현
- 사용자별 게시글 접근 권한 관리
- 게시글 검색 및 필터링 기능
- 댓글 및 대댓글 기능
- 게시글 조회수 관리
- 첨부 파일 업로드/다운로드

### 데이터베이스 설계 및 최적화
- H2 Database를 활용한 개발 환경 구축
- JPA를 활용한 엔티티 관계 설계
- 데이터 접근 성능 최적화
- 데이터 무결성 보장

## 📋 시스템 아키텍처

```
[클라이언트] <--> [Spring Boot 애플리케이션]
                        |
                        ▼
  [인증/인가: Spring Security + JWT + OAuth2]
                        |
                        ▼
  [비즈니스 로직: Service Layer]
                        |
                        ▼
    [데이터 접근: Spring Data JPA]
                        |
                        ▼
            [데이터베이스: H2/MySQL]
```

## 🔍 RESTful API 설계

| HTTP 메서드 | 엔드포인트             | 설명                     |
|------------|----------------------|------------------------|
| GET        | /api/posts           | 게시글 목록 조회          |
| GET        | /api/posts/{id}      | 특정 게시글 조회          |
| POST       | /api/posts           | 새 게시글 작성            |
| PUT        | /api/posts/{id}      | 게시글 수정               |
| DELETE     | /api/posts/{id}      | 게시글 삭제               |
| GET        | /api/posts/search    | 게시글 검색               |
| POST       | /api/auth/signup     | 회원가입                 |
| POST       | /api/auth/login      | 로그인                   |
| GET        | /api/auth/me         | 현재 인증된 사용자 정보     |

## ⚙️ 프로젝트 구조

```
src
├── main
│   ├── java
│   │   └── com.example.board
│   │       ├── config           # 설정 클래스
│   │       ├── controller       # REST 컨트롤러
│   │       ├── dto              # 데이터 전송 객체
│   │       ├── entity           # JPA 엔티티
│   │       ├── exception        # 예외 처리
│   │       ├── repository       # 데이터 접근 계층
│   │       ├── security         # 보안 관련 클래스
│   │       ├── service          # 비즈니스 로직
│   │       └── util             # 유틸리티 클래스
│   └── resources
│       ├── static               # 정적 자원
│       ├── templates            # Thymeleaf 템플릿
│       └── application.yml      # 애플리케이션 설정
└── test                         # 테스트 코드
```

## 🛠️ 설치 및 실행 방법

### 요구사항
- JDK 17 이상
- Maven 또는 Gradle
- Git

### 설치
```bash
# 저장소 복제
git clone https://github.com/yourusername/board-project.git

# 프로젝트 디렉토리로 이동
cd board-project

# Maven을 사용하는 경우
./mvnw clean install

# Gradle을 사용하는 경우
./gradlew build
```

### 실행
```bash
# Maven을 사용하는 경우
./mvnw spring-boot:run

# Gradle을 사용하는 경우
./gradlew bootRun
```

기본적으로 애플리케이션은 `http://localhost:8080`에서 실행됩니다.

## 📝 프로젝트 후기 및 개선점

### 구현 성과
- Spring Boot 기반의 웹 애플리케이션 아키텍처 설계 및 구현
- JWT와 OAuth2를 활용한 견고한 인증 시스템 구축
- RESTful API 설계 원칙 적용
- JPA를 활용한 효율적인 데이터 접근 계층 구현

### 개선 가능 사항
- 테스트 커버리지 확대
- 실시간 알림 기능 추가 (WebSocket 활용)
- 프런트엔드 프레임워크(React, Vue.js) 도입
- 성능 모니터링 및 최적화
- 도커화 및 CI/CD 파이프라인 구축

## 📜 라이센스

이 프로젝트는 MIT 라이센스에 따라 라이센스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 👥 기여 방법

1. 이 저장소를 포크합니다.
2. 기능 브랜치를 만듭니다 (`git checkout -b feature/amazing-feature`).
3. 변경 사항을 커밋합니다 (`git commit -m 'Add some amazing feature'`).
4. 브랜치에 푸시합니다 (`git push origin feature/amazing-feature`).
5. Pull Request를 엽니다.
