# Docker 가이드

Azit의 기본 실행 방식은 Docker Compose입니다.

## 서비스 구성

```text
azit           Spring Boot 애플리케이션
azit-database  MySQL 데이터베이스
```

## 실행

```powershell
docker-compose up --build
```

빌드에서 서버 구동까지 한번에 하려면 다음 명령어로도 가능합니다.

```powershell
docker-compose up -d --build
```

## 중지

```powershell
docker-compose stop
```

## 컨테이너 삭제

```powershell
docker-compose down
```

컨테이너와 네트워크는 삭제되지만, 데이터베이스 파일은 `./volumes/db` 아래에 남아 있습니다.

## 로그 확인

```powershell
docker-compose logs -f azit
docker-compose logs -f azit-database
```

## 로컬 데이터베이스 초기화

먼저 컨테이너를 종료합니다.

```powershell
docker-compose down
```

그 다음 로컬 데이터베이스 디렉터리를 삭제합니다.

```sh
rm -rf ./volumes/
```

## watch 모드

코드를 수정하는 대로 바꾼 점을 서버에서 실시간으로 보고 싶은 경우, watch 모드를 이용할 수 있습니다.

```powershell
docker-compose up --build
```
