# 🔑 Azit 프로젝트 초기 로그인 및 인증 시스템 구현 일지

---

## ✅ 주요 구현 사항

### 1. Spring Security 기본 설정

- **SecurityConfig 구성:** 폼 로그인을 활성화하고, `/register`, `/login`, `/css/**`, `/js/**` 등 공용 리소스를 허용했습니다.
- **BCrypt 비밀번호 암호화:** `PasswordEncoder`를 빈으로 등록하여 회원가입 시 비밀번호를 안전하게 해싱하여 저장하도록 구현했습니다.
- **커스텀 필터 체인:** 인가 오류 발생 시 로그인 페이지로 리다이렉트되도록 설정했습니다.

### 2. 사용자 데이터 모델 및 저장소 (초기 단계)

- **User 엔티티 (현 Member):** 이메일을 고유 식별자(Username)로 사용하도록 설계했습니다.
- **Repository Pattern:** `JpaRepository`를 상속받아 이메일로 사용자를 조회하는 기능을 구현했습니다.

### 3. 인증 로직 (Authentication)

- **CustomUserDetailsService:** Spring Security의 `UserDetailsService`를 인터페이스를 구현하여 DB의 이메일 데이터를 기반으로 사용자 정보를 로드하는 로직을 완성했습니다.
- **UserDetails 매핑:** DB의 유저 객체를 Spring Security가 이해할 수 있는 `User` 객체로 변환하여 세션 인증을 처리했습니다.

### 4. 회원가입 및 로그인 흐름

- **RegisterController:** 사용자의 입력값(이메일, 이름, 비밀번호)을 받아 중복 이메일 체크 후 DB에 저장하는 가입 프로세스를 구현했습니다.
- **LoginController:** 로그인 성공 및 실패 시의 경로를 제어하고, Thymeleaf 템플릿에 로그인 에러 메시지를 노출하도록 설정했습니다.

### 5. 데이터베이스 연동 (MySQL Transition)

- **Docker MySQL 연동:** 초기 H2에서 MySQL 8.0 환경으로 전환하여 영속성 있는 데이터 저장 환경을 구축했습니다.
- **환경 설정:** `application.properties`에 데이터소스 URL, 유저네임, 비밀번호 및 Hibernate DDL-Auto 옵션을 구성했습니다.

---

## 🛠 사용된 핵심 기술

- **Framework:** Spring Boot 4.0.5, Spring Security 6
- **Database:** MySQL 8.0 (Docker)
- **Library:** BCrypt Password Encoder, Spring Data JPA
- **Frontend:** Thymeleaf (Spring Security Extras 연동)

---

## 📝 참고 사항

_이후 작업에서 `User` 엔티티는 보다 명확한 도메인 용어 사용을 위해 `Member`로 리팩토링되었으며, `auth` 패키지와 `user` 패키지로 로직을 분리하였습니다._
