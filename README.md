# AZIT

AZIT는 프로젝트를 진행하기 위한 팀원을 매칭하고, 작업을 관리하는 서비스입니다.

## 빠른 시작

1. [Docker Desktop](https://docs.docker.com/get-started/introduction/get-docker-desktop/)을 설치해 Docker Engine과 Docker Compose 플러그인을 준비합니다.
2. [`.env.example`](./.env.example)을 참고해서 [`.env`](./.env) 파일을 생성합니다.
3. 다음 명령어로 애플리케이션과 데이터베이스를 실행합니다.

```sh
docker compose up --pull --wait -d
```

4. 브라우저에서 <http://localhost:8080>에 접속합니다.

## 환경변수

컨테이너를 원활히 사용하려면 .env 파일이 필요합니다. 다음과 같이 작성할 수 있습니다.

```env
# 필수
DB_USER = azit
DB_PASSWORD = azit1234
DB_ROOT_PASSWORD = azit1234

# 로컬
DB_URL = localhost
```

### 공통 필수

Docker 사용 여부와 관련 없이, 서버와 데이터베이스를 구동하려면 반드시 필요합니다.

 * `DB_USER`: 데이터베이스 사용자 이름
 * `DB_PASSWORD`: 데이터베이스 사용자 비밀번호
 * `DB_ROOT_PASSWORD`: 데이터베이스 root 비밀번호

### 로컬 필수

Docker를 사용하지 않고, Spring Boot 서버를 바로 실행할 때 필요합니다.

 * `DB_URL`: 데이터베이스 서버의 URL. 일반적으로 `localhost`

### Docker 선택

Docker compose를 사용할 때, 필요에 따라 변경할 수 있는 설정입니다.

 * `DB_DIR`: `./volumes/` 아래의 데이터베이스 디렉토리. 다른 데이터를 이용해 테스트를 하고자 할 때 사용합니다. (기본값: `db`)
 * `UPLOADS_DIR`: `./volumes/` 아래의 업로드 파일 디렉토리. 다른 데이터를 이용해 테스트를 하고자 할 때 사용합니다. (기본값: `uploads`)

### 로컬 선택

Docker를 사용하지 않고, Spring Boot 서버를 바로 실행할 때 필요에 따라 변경할 수 있는 설정입니다.

 * `DB_PORT`: 데이터베이스가 사용하는 포트 번호 (기본값: `3306`)
 * `DB_NAME`: 서버가 연결할 데이터베이스 이름 (기본값: `azit_db`)

## 문서

- [데이터베이스 가이드](docs/database.md)
- [공용 GCP 데이터베이스 접속 가이드](docs/shared-gcp-db.md)
- [Docker 가이드](docs/docker.md)
- [인증 구현 기록](docs/development-log/2026-04-04-auth.md)
- [작업 진행 기록](docs/development-log/2026-04-04-progress.md)
