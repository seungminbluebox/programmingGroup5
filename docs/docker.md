# Docker 가이드

Azit의 기본 실행 방식은 Docker Compose입니다.

## 서비스 구성

```text
azit           Spring Boot 애플리케이션
azit-database  MySQL 데이터베이스
```

## 실행

```powershell
docker compose up --build
```

백그라운드에서 실행하려면 다음 명령어를 사용합니다.

```powershell
docker compose up -d --build
```

## 중지

```powershell
docker compose stop
```

## 컨테이너 삭제

```powershell
docker compose down
```

컨테이너와 네트워크는 삭제되지만, 데이터베이스 파일은 `./volumes/db` 아래에 남아 있습니다.

## 로그 확인

```powershell
docker compose logs -f azit
docker compose logs -f azit-database
```

## 로컬 데이터베이스 초기화

먼저 컨테이너를 종료합니다.

```powershell
docker compose down
```

그 다음 로컬 데이터베이스 디렉터리를 삭제합니다.

```powershell
Remove-Item -Recurse -Force .\volumes\db
```

다시 실행합니다.

```powershell
docker compose up --build
```

초기화는 로컬 DB를 완전히 새로 만들고 싶을 때만 사용합니다.
