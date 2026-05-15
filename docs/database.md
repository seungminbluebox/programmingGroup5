# 데이터베이스 가이드

Azit은 Docker 기반 MySQL을 사용합니다. DB dump 파일을 별도로 공유하지 않는 한, 데이터베이스 데이터는 각 팀원의 로컬 환경에 따로 존재합니다.

팀 공용 GCP DB를 사용할 때는 [공용 GCP 데이터베이스 접속 가이드](shared-gcp-db.md)를 따릅니다.

## 접속 정보

호스트 PC에서 접속할 때의 정보입니다.

```text
호스트: localhost
포트: 3306
데이터베이스: azit_db
사용자: azit
비밀번호: azit1234
```

실행 중인 컨테이너를 통해 MySQL에 접속합니다.

```powershell
docker exec -it azit-azit-database-1 mysql -u azit -p azit_db
```

비밀번호는 다음과 같습니다.

```text
azit1234
```

## 로컬 저장 위치

Docker Compose는 MySQL 데이터를 아래 경로에 저장합니다.

```text
./volumes/db
```

`volumes/`는 Git에서 제외되어 있으므로, 이 데이터는 저장소를 통해 공유되지 않습니다.

## 테이블 구조

`members`는 회원 인증 정보, 프로필, 권한, 매너 온도, 검색 상태, SNS 링크를 저장합니다.

주요 컬럼:

```text
id, email, password, name, age, bio, profile_url, manner_temp, is_searching, role, sns_links
```

`tech_stacks`는 관리자가 등록하는 기술 스택 목록을 저장합니다.

주요 컬럼:

```text
id, stack_name, category, icon_url
```

`member_stacks`는 회원과 기술 스택을 연결하고 경험 연수를 저장합니다.

주요 컬럼:

```text
id, member_id, stack_id, exp_years
```

`careers`는 회원의 경력 또는 프로젝트 항목을 저장합니다.

주요 컬럼:

```text
id, member_id, title, description, link_url
```

`availabilities`는 회원이 활동 가능한 요일과 시간대를 저장합니다.

주요 컬럼:

```text
id, member_id, day_of_week, start_time, end_time
```

## 자주 쓰는 SQL

```sql
SHOW TABLES;
SELECT id, email, name, role FROM members;
SELECT * FROM tech_stacks;
UPDATE members SET role = 'ROLE_ADMIN' WHERE id = 1;
```

## 데이터 공유

각 개발자는 별도의 로컬 DB를 사용합니다. 데이터를 공유하려면 dump 파일을 만들어 팀원에게 전달해야 합니다.

내보내기:

```powershell
docker exec azit-azit-database-1 mysqldump -u azit -pazit1234 azit_db > azit_db_dump.sql
```

가져오기:

```powershell
Get-Content .\azit_db_dump.sql | docker exec -i azit-azit-database-1 mysql -u azit -pazit1234 azit_db
```
