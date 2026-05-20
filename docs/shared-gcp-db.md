# 공용 GCP 데이터베이스 접속 가이드

이 문서는 팀원들이 각자 로컬에서 Azit 애플리케이션을 실행하되, 데이터베이스는 Google Cloud VM의 공용 MySQL을 사용하기 위한 가이드입니다.

## 구조

```text
소스코드: 각자 로컬 프로젝트
애플리케이션: 각자 로컬에서 Spring Boot 실행
데이터베이스: GCP Compute Engine VM의 Docker MySQL
```

공용 DB 정보:

```text
DB 호스트: 34.127.52.119
DB 포트: 3306
DB 이름: azit_db
DB 사용자: azit
```

비밀번호는 팀 내부에서 별도로 공유받으세요. 비밀번호를 Git, 문서, 채팅방 공개 공간에 올리지 마세요.

## 1. 내 공인 IP 확인

PowerShell에서 실행합니다.

```powershell
Invoke-WebRequest -UseBasicParsing https://ifconfig.me
```

또는 브라우저에서 접속합니다.

```text
https://ifconfig.me/ip
```

나온 IP를 DB 관리자에게 전달합니다. 관리자는 Google Cloud 방화벽 규칙 `allow-mysql-azit`의 소스 IPv4 범위에 아래 형식으로 추가해야 합니다.

```text
내공인IP/32
```

예:

```text
175.195.67.61/32
```

## 2. DB 포트 연결 확인

관리자가 IP를 추가한 뒤, 로컬 PowerShell에서 확인합니다.

```powershell
Test-NetConnection -ComputerName 34.19.2.180 -Port 3306
```

성공하면 아래처럼 나옵니다.

```text
TcpTestSucceeded : True
```

`False`가 나오면 다음 중 하나입니다.

- 현재 공인 IP가 방화벽에 등록되지 않았습니다.
- 카페, 학교, 핫스팟 등 네트워크 변경으로 공인 IP가 바뀌었습니다.
- GCP VM 또는 MySQL 컨테이너가 꺼져 있습니다.
- VM의 네트워크 태그에 `azit-db`가 빠져 있습니다.

## 3. 로컬 환경변수 설정

프로젝트 루트의 `.env` 파일을 아래처럼 설정합니다.

```env
DB_URL=34.19.2.180
DB_PORT=3306
DB_NAME=azit_db
DB_USER=azit
DB_PASSWORD=팀에서_공유받은_비밀번호
```

`.env`는 Git에 올리면 안 됩니다.

## 4. IDE 실행 시 주의사항

Spring Boot는 `.env` 파일을 자동으로 읽지 않을 수 있습니다. VS Code나 IntelliJ에서 실행했는데 로그에 아래 오류가 나오면 환경변수가 적용되지 않은 것입니다.

```text
UnknownHostException: azit-database
```

가장 확실한 실행 방법은 PowerShell에서 환경변수를 설정한 뒤, 같은 PowerShell에서 IDE를 여는 것입니다.

```powershell
$env:DB_URL='34.19.2.180'
$env:DB_PORT='3306'
$env:DB_NAME='azit_db'
$env:DB_USER='azit'
$env:DB_PASSWORD='팀에서_공유받은_비밀번호'

cd "프로젝트_경로\azit"
code .
```

그 다음 IDE에서 `AzitApplication`을 실행합니다.

## 5. 애플리케이션 접속

로컬에서 Spring Boot가 정상 실행되면 브라우저에서 접속합니다.

```text
http://localhost:8080
```

회원가입, 로그인, 프로필 수정 데이터는 공용 GCP DB에 저장됩니다.

## 6. DB 직접 접속

로컬 PC에 MySQL 클라이언트가 설치되어 있으면 다음 명령어로 접속할 수 있습니다.

```powershell
mysql -h 34.19.2.180 -P 3306 -u azit -p azit_db
```

GCP VM 안에서 접속하려면 SSH 터미널에서 실행합니다.

```bash
docker exec -it azit-mysql mysql -u azit -p azit_db
```

자주 쓰는 확인 SQL:

```sql
SHOW TABLES;
SELECT id, email, name, role FROM members;
SELECT category, stack_name FROM tech_stacks ORDER BY category, stack_name;
```

## 7. 관리자 체크리스트

GCP 설정은 아래 상태여야 합니다.

```text
VM 이름: azit
VM 외부 IP: 34.19.2.180
VM 네트워크 태그: azit-db
방화벽 규칙 이름: allow-mysql-azit
방화벽 방향: 수신
방화벽 대상 태그: azit-db
방화벽 프로토콜/포트: tcp:3306
방화벽 소스 IPv4 범위: 팀원공인IP/32
```

VM에서 Docker 포트 매핑도 확인합니다.

```bash
docker ps
```

`azit-mysql` 컨테이너에 아래 포트 매핑이 보여야 합니다.

```text
0.0.0.0:3306->3306/tcp
```

## 8. 주의사항

- 공용 DB이므로 한 명이 삭제한 데이터는 다른 팀원에게도 영향을 줍니다.
- 실수로 데이터를 지우기 전에 SQL을 확인하세요.
- 네트워크를 바꾸면 공인 IP가 바뀔 수 있습니다.
- IP가 바뀌면 방화벽 규칙에 새 IP를 다시 추가해야 합니다.
- `0.0.0.0/0`으로 MySQL 포트를 열지 마세요.
