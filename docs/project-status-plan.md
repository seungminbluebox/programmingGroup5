# Project Status Feature Plan

## Purpose

프로젝트 정보 기능에 프로젝트 상태를 추가하고, 상태에 따라 수정 가능 여부를 제어한다. 이번 구현은 기능 범위를 두 단계로 나누어 진행한다.

- 1단계: 프로젝트 상태 관리와 수정 잠금 정책 구현
- 2단계: 팀원 초대 방식을 ProjectInvitation 기반으로 통일

이 문서는 본격적인 개발 전에 확정한 1단계 구현 범위와 정책을 정리한다.

## Confirmed Decisions

### Project Deletion

- 프로젝트 삭제는 완전 삭제로 유지한다.
- 삭제는 프로젝트 오너만 가능하다.
- 프로젝트 상태가 진행중, 완료, 중단 중 무엇이든 오너는 삭제할 수 있다.
- 삭제 시 기존처럼 초대, 태스크, 링크, 멤버십 데이터를 먼저 삭제한 뒤 프로젝트를 삭제한다.

### Project Status

- 프로젝트 상태는 enum으로 저장한다.
- 상태값은 다음 세 가지로 정의한다.

| Enum | Label |
| --- | --- |
| `IN_PROGRESS` | 진행중 |
| `COMPLETED` | 완료 |
| `PAUSED` | 중단 |

- 새 프로젝트의 기본 상태는 `IN_PROGRESS`이다.
- 기존 프로젝트 데이터도 전부 `IN_PROGRESS`로 간주한다.
- DB 컬럼은 `NOT NULL`로 설계한다.
- 상태 변경 이력은 별도 테이블로 저장하지 않는다.
- 상태 변경 시 `Project.updatedAt`은 갱신되도록 둔다.

### Status Permission

- 프로젝트 상태 변경은 오너만 가능하다.
- 오너는 모든 상태에서 상태 변경 폼을 볼 수 있다.
- 상태 전환은 모든 방향으로 허용한다.

허용되는 예:

- 진행중 -> 완료
- 진행중 -> 중단
- 완료 -> 진행중
- 완료 -> 중단
- 중단 -> 진행중
- 중단 -> 완료

### Edit Lock Policy

- `IN_PROGRESS` 상태에서는 기존 수정 기능을 사용할 수 있다.
- `COMPLETED`, `PAUSED` 상태에서는 프로젝트를 조회만 할 수 있다.
- 완료/중단 상태에서도 오너는 상태 변경과 프로젝트 삭제를 할 수 있다.
- 완료/중단 상태에서는 다음 수정 작업을 막는다.

1. 작업 링크 추가
2. 팀원 직접 추가
3. 초대 보내기
4. 태스크 추가
5. 태스크 완료 토글
6. 초대 수락

서비스 계층에는 공통 검증 메서드를 둔다.

```java
private void assertProjectEditable(Project project) {
    if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
        throw new IllegalArgumentException("완료 또는 중단된 프로젝트는 수정할 수 없습니다.");
    }
}
```

상태 변경과 프로젝트 삭제에는 이 검증을 적용하지 않는다.

### Completion Warning

- 프로젝트를 `COMPLETED`로 변경할 때 미완료 태스크가 있어도 완료 자체는 허용한다.
- 단, 화면에서 confirm 경고를 보여준다.

예상 문구:

```text
미완료 태스크가 있습니다. 그래도 완료하시겠습니까?
```

- 이 경고는 프론트의 `confirm()`으로 처리한다.
- 서버는 미완료 태스크를 이유로 완료 상태 변경을 차단하지 않는다.

## UI Policy

### Project Detail

프로젝트 상세 화면의 제목 영역에 상태를 표시한다.

- 프로젝트 이름 옆 또는 아래에 상태 배지를 보여준다.
- 오너에게만 상태 변경 select와 변경 버튼을 보여준다.
- 일반 멤버는 상태 배지만 볼 수 있다.
- 상태 변경 select에는 현재 상태도 포함한다.
- 같은 상태로 제출해도 에러로 처리하지 않는다.

상태 배지 색상:

| Status | Color |
| --- | --- |
| 진행중 | 파랑 |
| 완료 | 청록 또는 초록 |
| 중단 | 회색 |

### Read-only Notice

완료 또는 중단 상태에서는 프로젝트 상세 상단에 안내 문구를 한 번만 표시한다.

예상 문구:

```text
완료 또는 중단된 프로젝트는 조회만 가능합니다. 오너가 상태를 진행중으로 변경하면 다시 수정할 수 있습니다.
```

각 카드마다 반복 문구를 넣지는 않는다.

### Hidden Forms and Buttons

완료/중단 상태에서는 수정 form과 버튼을 숨긴다.

- 팀원 추가 버튼/폼 숨김
- 링크 추가 폼 숨김
- 태스크 추가 폼 숨김
- 태스크 완료 토글 버튼 숨김

태스크 목록에서는 완료 여부를 버튼 대신 텍스트나 배지로 표시한다.

예:

```text
상태: 완료
상태: 미완료
```

### Dashboard

- 홈/대시보드의 프로젝트 목록에는 상태 배지만 표시한다.
- 상태별 필터는 1단계 범위에서 제외한다.

### Flash Message

프로젝트 상세 화면 상단에 공통 flash 메시지 영역을 둔다.

- 성공 메시지: `successMessage`
- 실패 메시지: `errorMessage`

상태 변경 실패, 권한 오류, 수정 불가 오류 등을 한 곳에서 보여준다.

## Controller Policy

상태 변경 URL은 다음으로 정한다.

```text
POST /project/{projectId}/status
```

요청 파라미터:

```text
status=IN_PROGRESS | COMPLETED | PAUSED
```

예상 컨트롤러 형태:

```java
@PostMapping("/project/{projectId}/status")
public String updateStatus(
        Authentication authentication,
        @PathVariable Long projectId,
        @RequestParam ProjectStatus status,
        RedirectAttributes redirectAttributes) {
    projectService.updateProjectStatus(authentication.getName(), projectId, status);
    return "redirect:/project/" + projectId;
}
```

실제 구현에서는 예외를 잡아 `errorMessage`로 redirect한다.

## Phase 1 Scope

1단계 구현 범위는 다음으로 확정한다.

1. `ProjectStatus` enum 추가
2. `Project.status` 필드 추가
3. 기본 상태 `IN_PROGRESS` 설정
4. `ProjectService.updateProjectStatus(email, projectId, status)` 추가
5. 상태 변경은 오너만 가능하도록 검증
6. `ProjectService.assertProjectEditable(project)` 추가
7. 완료/중단 상태에서 수정성 서비스 메서드 차단
8. `deleteProject`는 상태와 관계없이 오너면 가능하게 유지
9. `project_detail.html` 제목 영역에 상태 배지와 오너용 상태 변경 폼 추가
10. 완료/중단 상태 안내 문구 상단 표시
11. 완료/중단 상태에서는 수정 form/button 숨김
12. 태스크 토글 버튼은 완료/중단 상태에서 숨기고 완료 여부만 표시
13. `home.html` 프로젝트 목록에 상태 배지 표시
14. `ProjectServiceTest`에 상태 관련 단위 테스트 추가
15. 전체 `mvn test` 통과

## Phase 2 Scope

2단계는 초대 방식을 정리한다. 1단계에서는 구현하지 않는다.

확정된 2단계 방향:

- 팀원 추가는 `ProjectInvitation` 기반으로 통일한다.
- 추천 후보 목록에서 초대할 수 있다.
- 이메일 직접 입력으로도 초대할 수 있다.
- 두 방식 모두 `ProjectInvitation(PENDING)`을 생성한다.
- 상대방이 초대를 수락해야 `ProjectMember`가 생성된다.
- 오너만 초대 가능하다.
- 존재하지 않는 이메일은 명확하게 안내한다.

예상 오류 문구:

```text
가입된 회원을 찾을 수 없습니다.
이미 프로젝트에 참여 중인 회원입니다.
이미 초대를 보낸 회원입니다.
자기 자신에게는 초대할 수 없습니다.
완료 또는 중단된 프로젝트에는 초대를 보낼 수 없습니다.
```

기존 `addMemberToProject` public 기능은 제거하거나 미사용 처리한다. 멤버십 생성 로직은 초대 수락 내부에서 private helper로만 유지한다.

예상 구조:

```java
public void sendInvitationByEmail(String senderEmail, Long projectId, String receiverEmail)

private ProjectMember addProjectMember(Project project, Member member)
```

## Test Plan

1단계는 서비스 단위 테스트 중심으로 검증한다.

필수 테스트:

1. `createProject` 생성 시 기본 상태가 `IN_PROGRESS`
2. 오너는 프로젝트 상태를 변경할 수 있음
3. 일반 멤버는 프로젝트 상태를 변경할 수 없음
4. 완료 상태에서는 링크 추가 불가
5. 중단 상태에서는 링크 추가 불가
6. 완료/중단 상태에서는 팀원 추가 불가
7. 완료/중단 상태에서는 초대 발송 불가
8. 완료/중단 상태에서는 태스크 추가 불가
9. 완료/중단 상태에서는 태스크 토글 불가
10. 완료/중단 상태에서는 초대 수락 불가
11. 완료/중단 상태에서도 오너는 프로젝트 삭제 가능
12. 같은 상태로 변경 요청해도 에러 없이 처리

컨트롤러와 Thymeleaf 화면은 이번 범위에서는 별도 자동 테스트를 추가하지 않고, 빌드와 수동 확인으로 검증한다.

## Out of Scope

1단계에서 제외하는 항목:

- 상태별 대시보드 필터
- 상태 변경 이력 저장
- 상태 변경자 기록
- 소프트 삭제
- 이메일 기반 초대 통일 구현
- 초대 관련 컨트롤러 테스트
- 프로젝트 일정 기능
