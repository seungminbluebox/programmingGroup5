# Azit

Azit은 회원가입, 로그인, 프로필 관리, 기술 스택, 경력, 활동 가능 시간대를 관리하는 Spring Boot 웹 애플리케이션입니다.

## 빠른 시작

1. Docker Desktop과 JDK 25를 설치합니다.
2. `.env.example`을 참고해서 `.env` 파일을 생성합니다.
3. 애플리케이션과 데이터베이스를 실행합니다.

```powershell
docker-compose up --build
```

4. 브라우저에서 접속합니다.

```text
http://localhost:8080
```

## 로컬 개발

팀 공통 기본 실행 방식은 Docker Compose입니다. Spring Boot 애플리케이션과 MySQL을 같은 설정으로 실행할 수 있어 각 팀원이 동일한 로컬 개발 환경을 만들 수 있습니다.

IDE에서 Spring Boot를 직접 실행할 때는 MySQL 컨테이너만 Docker로 실행한 뒤, 아래 환경변수를 설정해야 합니다.

```powershell
$env:DB_URL='localhost'
$env:DB_USER='azit'
$env:DB_PASSWORD='azit1234'
```

## 문서

- [개발 환경 설정 가이드](docs/setup.md)
- [데이터베이스 가이드](docs/database.md)
- [공용 GCP 데이터베이스 접속 가이드](docs/shared-gcp-db.md)
- [Docker 가이드](docs/docker.md)
- [인증 구현 기록](docs/development-log/2026-04-04-auth.md)
- [작업 진행 기록](docs/development-log/2026-04-04-progress.md)
