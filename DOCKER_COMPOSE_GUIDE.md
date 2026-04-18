# docker compose 사용 방법

Docker Compose를 사용하면 Spring Boot 서버와 MySQL 데이터베이스 컨테이너를 한번에 관리할 수 있습니다.

## docker 및 docker compose 설치

다음 명령어를 사용하려면 Docker Engine과 Docker Compose 플러그인이 설치되어야 합니다.

Docker Desktop을 설치하면 Docker Engine과 Docker Compose가 모두 설치됩니다. Docker Desktop 설치 방법은 [여기](https://docs.docker.com/get-started/introduction/get-docker-desktop/)를 참고해 주세요.

## .env 세팅

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


## 빌드 (최초 실행)

코드 수정 후 최초 실행 전에 Docker 이미지 빌드를 진행해야 합니다. 다음 명령어로 진행할 수 있습니다.

```sh
docker compose build
```

빌드에서 서버 구동까지 한번에 하려면 다음 명령어로도 가능합니다.

```sh
docker compose up --wait
```

## 서버 구동

Docker를 이용해 데이터베이스와 서버를 한번에 구동하려면 다음 명령어를 이용하면 됩니다.

```sh
docker compose up --wait
```

서버가 구동되는 동안 Spring Boot 서버와 MySQL 데이터베이스의 로그가 터미널에 표시됩니다. 여기서 Ctrl+C를 누르면 서버를 종료할 수 있습니다.

만약, 로그를 확인할 필요가 없고 서버를 구동하는 동안 다른 명령어를 사용해야 한다면 다음 명령어로도 서버를 구동할 수 있습니다. 서버가 시작되는대로 터미널에서 다음 명령어를 사용할 수 있는 상태가 되며, 서버는 백그라운드에서 작동합니다.

```sh
docker compose up -d --wait
```

## 서버 정지

백그라운드에서 구동 중인 서버를 중단하려면 다음 명령어를 사용하면 됩니다.

```sh
docker compose stop
```

## 컨테이너 삭제

서버 컨테이너를 삭제해야 하는 경우, 다음 명령어를 사용할 수 있습니다. 컨테이너를 삭제하더라도 데이터베이스 데이터는 `./volumes/` 경로에 남아 컨테이너를 다시 구동하면 바로 사용할 수 있습니다.

```sh
docker compose down -v
```

## watch 모드

코드를 수정하는 대로 바꾼 점을 서버에서 실시간으로 보고 싶은 경우, watch 모드를 이용할 수 있습니다.

```sh
docker compose up -d --wait &&
docker compose up --watch
```
