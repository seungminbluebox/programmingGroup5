# 개발 환경 설정 가이드

이 문서는 Azit 팀원이 로컬 환경에서 프로젝트를 실행하기 위한 기준 설정 가이드입니다.

팀 공용 GCP DB를 사용할 때는 [공용 GCP 데이터베이스 접속 가이드](shared-gcp-db.md)를 먼저 확인하세요.

## 사전 준비사항

- Docker Desktop
- JDK 25
- PowerShell, CMD 또는 IDE에서 제공하는 터미널

## 환경변수 파일

프로젝트 루트에 `.env` 파일을 생성합니다. `.env.example`을 복사해서 만들 수 있습니다.

```env
DB_USER=azit
DB_PASSWORD=azit1234
DB_ROOT_PASSWORD=azit1234
DB_NAME=azit_db
DB_URL=localhost
```

`DB_URL=localhost`는 호스트 PC에서 Spring Boot를 직접 실행할 때 사용합니다. Docker Compose로 애플리케이션 컨테이너를 실행하는 경우에는 내부 Docker 서비스 이름인 `azit-database`가 기본값으로 사용됩니다.

## Docker Compose로 실행

프로젝트 루트에서 실행합니다.

```powershell
docker-compose up --build
```

애플리케이션 접속 주소는 다음과 같습니다.

```text
http://localhost:8080
```

## Spring Boot 직접 실행

데이터베이스만 Docker로 먼저 실행합니다.

```powershell
docker-compose up -d azit-database
```

IDE에서 `AzitApplication`을 실행하기 전에 환경변수를 설정합니다.

```powershell
$env:DB_URL='localhost'
$env:DB_USER='azit'
$env:DB_PASSWORD='azit1234'
```

그 다음 아래 메인 클래스를 실행합니다.

```text
kr.ac.dankook.group5.azit.AzitApplication
```

## 주요 페이지

- `/`, `/home`: 홈
- `/register`: 회원가입
- `/login`: 로그인
- `/profile/me`: 현재 로그인한 회원의 프로필
- `/profile/{id}`: 회원 ID 기반 프로필
- `/admin/stacks`: 기술 스택 관리, 관리자 전용
