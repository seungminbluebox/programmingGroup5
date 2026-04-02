# Azit 프로젝트 로컬 데이터베이스 셋업 가이드

본 문서는 팀원들이 로컬 환경에서 Azit 프로젝트를 실행하기 위한 **MySQL 데이터베이스(Docker) 셋업 가이드**입니다.
현재 프로젝트의 DB는 Docker를 통해 일관된 환경으로 구축되어 있습니다.

---

## 🚀 1. 사전 준비사항 (Prerequisites)

- **Docker Desktop**이 설치되어 있고 실행 중이어야 합니다. [다운로드 링크](https://www.docker.com/products/docker-desktop)
- 프로젝트 코드를 클론(Clone) 받고 IDE(예: VS Code, IntelliJ)로 열어둔 상태여야 합니다.

---

## ⚙️ 2. Docker로 MySQL 컨테이너 실행하기

복잡한 MySQL 설치 과정 없이, 아래 명령어 한 줄로 DB 실행 및 설정이 완료됩니다.
터미널(Terminal) 또는 PowerShell을 열고 아래 명령어를 복사하여 실행해 주세요.

```bash
docker run --name azit-mysql -e MYSQL_ROOT_PASSWORD=azit1234 -e MYSQL_DATABASE=azit_db -p 3306:3306 -d mysql:8.0
```

### 명령어 상세 설명:

- `--name azit-mysql`: 컨테이너 이름을 `azit-mysql`로 지정
- `-e MYSQL_ROOT_PASSWORD=azit1234`: **Root 계정 비밀번호**를 `azit1234`로 설정
- `-e MYSQL_DATABASE=azit_db`: 컨테이너 실행 시 **azit_db라는 데이터베이스(스키마) 자동 생성**
- `-p 3306:3306`: 사용할 포트 (로컬 3306 포트를 컨테이너 3306 포트에 연결)
- `-d mysql:8.0`: MySQL 8.0 버전을 백그라운드 서버로 실행

> **실행 확인:** `docker ps` 명령어를 입력했을 때, `azit-mysql` 컨테이너가 `Up` 상태이면 성공입니다.

---

## 🏃 3. 스프링 부트 프로젝트와 연동 확인

**1) `application.properties` 확인**
프로젝트 소스의 `src/main/resources/application.properties` 내용이 다음과 같은지 확인하세요.
_(이 파일은 Git에 푸시되어 있어야 합니다.)_

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/azit_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=azit1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 자동 테이블 생성
spring.jpa.hibernate.ddl-auto=update
```

**2) 애플리케이션 실행**
스프링 부트 애플리케이션(`AzitApplication.java`)을 실행하세요.
실행 과정에서 에러 없이 **`Tomcat started on port 8080`** 로그가 출력되고, `users` 테이블 생성 쿼리(`create table users ...`)가 콘솔에 보인다면 연동이 완벽하게 완료된 것입니다.

---

## 📝 5. DB 데이터 직접 확인 및 관리하기

도커로 실행 중인 MySQL에 직접 접속하여 데이터를 확인하고 관리하는 방법입니다.

### 1) MySQL 컨테이너 내부 접속

터미널(PowerShell 또는 CMD)에서 아래 명령어를 입력하여 MySQL 클라이언트를 실행합니다.

```bash
docker exec -it azit-mysql mysql -u root -p azit_db
```

- **비밀번호 입력:** `azit1234` (입력 시 화면에 글자가 표시되지 않으니 주의하세요.)

### 2) 유용한 SQL 명령어 (접속 후 `mysql>` 프롬프트에서 입력)

모든 SQL 명령어 끝에는 세미콜론(`;`)을 반드시 붙여야 합니다.

- **전체 회원 목록 조회:**
  ```sql
  SELECT * FROM users;
  ```
- **특정 이메일을 가진 회원 삭제:**
  ```sql
  DELETE FROM users WHERE email = 'test@example.com';
  ```
- **테이블 구조 확인 (인코딩 등):**
  ```sql
  DESC users;
  -- 또는 상세 정보 보기
  SHOW CREATE TABLE users;
  ```
- **모든 데이터 삭제 (초기화):**
  ```sql
  DELETE FROM users;
  ```

### 3) 접속 종료

확인이 끝나면 아래 명령어로 빠져나옵니다.

```sql
exit
```

---

## 💡 자주 발생하는 문제 해결 (Troubleshooting)

### Q: `docker run` 시 "Conflict" 에러가 발생합니다.

이미 `azit-mysql`이라는 이름의 컨테이너가 생성된 상태입니다. 터미널을 다시 켜거나 컴퓨터를 재부팅한 경우라면 아래 명령어로 **이미 생성된 컨테이너를 시작**만 해주면 됩니다.

```bash
docker start azit-mysql
```

### Q: 터미널에서 닉네임이 `??`로 보입니다.

이는 터미널(PowerShell 등)이 한글을 제대로 표시하지 못하는 설정일 때 발생합니다. 웹 브라우저(`http://localhost:8080`)에서 닉네임이 정상적으로 출력된다면 데이터베이스 저장은 완벽하게 성공한 것이니 안심하셔도 됩니다.
