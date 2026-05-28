package kr.ac.dankook.group5.azit.schedule;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MergeIntervalsTimeOverlapManagerTest {
    private TimeRangeOverlapManager timeRangeOverlapManager = new MergeIntervalsTimeRangeOverlapManager();

    // 정수 시(hour)로 간단히 Schedule을 생성하는 헬퍼
    private TimeRange s(int startH, int endH) {
        return new TimeRange(LocalTime.of(startH, 0), LocalTime.of(endH, 0));
    }

    // =========================================================
    // combineOccupiedSchedules 테스트
    // =========================================================

    /**
     * TC-1: 빈 입력 → 빈 결과
     *
     * 목적: 아무 일정도 없을 때 예외 없이 빈 리스트를 반환하는지 확인
     *
     * 입력: {}
     * 결과: []
     */
    @Test
    void combineOccupied_emptySet_returnsEmptyList() {
        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of());

        assertThat(result).isEmpty();
    }

    /**
     * TC-2: 일정 1개 → 그대로 반환
     *
     * 목적: 병합 대상이 없을 때 단일 일정이 그대로 나오는지 확인
     *
     * 입력: { 9-12 }
     * 결과: [ 9-12 ]
     */
    @Test
    void combineOccupied_singleSchedule_returnsSelf() {
        TimeRange schedule = s(9, 12);

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of(schedule));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(schedule);
    }

    /**
     * TC-3: 겹치지 않는 여러 일정 → 시작 시간 순으로 정렬해 반환
     *
     * 목적: 겹치지 않는 구간들은 병합하지 않고 시작 시간 순으로 반환하는지 확인
     * 입력 Set 순서와 무관하게 항상 정렬된 순서로 나와야 함
     *
     * 입력: { 14-17, 9-12 } (역순 입력)
     * 결과: [ 9-12, 14-17 ]
     */
    @Test
    void combineOccupied_nonOverlappingSchedules_returnsSortedByStartTime() {
        TimeRange morning = s(9, 12);
        TimeRange afternoon = s(14, 17);
        // 역순으로 넣어도 정렬되어 나와야 한다
        Set<TimeRange> schedules = new HashSet<>(Set.of(afternoon, morning));

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(schedules);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(morning);
        assertThat(result.get(1)).isEqualTo(afternoon);
    }

    /**
     * TC-4: 겹치는 두 구간 → 하나로 병합
     *
     * 목적: 두 구간이 일부 겹칠 때 더 넓은 구간 하나로 합쳐지는지 확인
     *
     * 입력: { 9-14, 11-17 }
     * 결과: [ 9-17 ]
     */
    @Test
    void combineOccupied_partiallyOverlapping_mergesIntoOne() {
        TimeRange a = s(9, 14);
        TimeRange b = s(11, 17);

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of(a, b));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    /**
     * TC-5: 포함 관계 (한 구간이 다른 구간을 완전히 포함) → 큰 구간만 반환
     *
     * 목적: 큰 구간 안에 작은 구간이 완전히 들어갈 때 큰 구간 하나만 반환하는지 확인
     *
     * 입력: { 9-17, 11-14 }
     * 결과: [ 9-17 ]
     */
    @Test
    void combineOccupied_containedSchedule_returnsOuterOnly() {
        TimeRange outer = s(9, 17);
        TimeRange inner = s(11, 14);

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of(outer, inner));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    /**
     * TC-6: 시작 시간이 같은 두 구간 → 더 긴 쪽으로 병합
     *
     * 목적: 두 구간의 시작 시간이 같을 때 더 늦은 종료 시간을 가진 구간이 선택되는지 확인
     *
     * 입력: { 9-12, 9-17 }
     * 결과: [ 9-17 ]
     */
    @Test
    void combineOccupied_sameStartTime_keepsLonger() {
        TimeRange shorter = s(9, 12);
        TimeRange longer = s(9, 17);

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of(shorter, longer));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    /**
     * TC-7: 연속된 구간 (끝과 시작이 맞닿음) → 하나로 병합
     *
     * 목적: 한 구간의 종료 시간과 다음 구간의 시작 시간이 정확히 같을 때 하나로 합쳐지는지 확인
     *
     * 입력: { 9-12, 12-17 }
     * 결과: [ 9-17 ]
     */
    @Test
    void combineOccupied_adjacentSchedules_mergesIntoOne() {
        TimeRange first = s(9, 12);
        TimeRange second = s(12, 17);

        List<TimeRange> result = timeRangeOverlapManager.combineOccupiedSchedules(Set.of(first, second));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    // =========================================================
    // findAvailableSchedules 테스트
    // =========================================================

    /**
     * TC-8: 빈 입력 → 하루 전체가 가용
     *
     * 목적: 점유된 일정이 없을 때 자정~자정(하루 전체)을 가용으로 반환하는지 확인
     *
     * 점유: (없음)
     * 가용: |━━━━━━━━━━━━━━━━━━━━━━━━| (00:00 ~ 23:59:59)
     */
    @Test
    void findAvailable_emptySet_returnsFullDay() {
        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.MIN);
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.MAX);
    }

    /**
     * TC-9: 중간 시간대 1개 점유 → 그 앞과 뒤를 반환
     *
     * 목적: 하루 중 일부 시간만 점유될 때 점유 구간 앞과 뒤를 각각 가용으로 반환하는지 확인
     *
     * 점유: |━━━━━━━━━|
     * 09:00 17:00
     * 가용: |━━━━━| |━━━━━━━|
     * 00:00 09:00 17:00 23:59
     */
    @Test
    void findAvailable_singleOccupiedInMiddle_returnsBeforeAndAfter() {
        TimeRange occupied = s(9, 17);

        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of(occupied));

        assertThat(result).hasSize(2);

        // 앞 구간: [MIN, 9:00)
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.MIN);
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(9, 0));

        // 뒤 구간: [17:00, MAX]
        assertThat(result.get(1).getStartTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(result.get(1).getEndTime()).isEqualTo(LocalTime.MAX);
    }

    /**
     * TC-10: 여러 구간 점유 → 구간 사이의 빈 시간을 모두 반환
     *
     * 목적: 점유 구간이 여러 개일 때 각 구간 사이의 빈 시간을 빠짐없이 반환하는지 확인
     *
     * 점유: |━━━| |━━━|
     * 09 12 14 17
     * 가용: |━━━━━| |━━━| |━━━━━━━|
     * 00:00 09 12 14 17 23:59
     */
    @Test
    void findAvailable_multipleOccupied_returnsAllGaps() {
        TimeRange morning = s(9, 12);
        TimeRange afternoon = s(14, 17);

        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of(morning, afternoon));

        assertThat(result).hasSize(3);

        // 앞 구간: [MIN, 9:00)
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.MIN);
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(9, 0));

        // 중간 구간: [12:00, 14:00)
        assertThat(result.get(1).getStartTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(result.get(1).getEndTime()).isEqualTo(LocalTime.of(14, 0));

        // 뒤 구간: [17:00, MAX]
        assertThat(result.get(2).getStartTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(result.get(2).getEndTime()).isEqualTo(LocalTime.MAX);
    }

    /**
     * TC-11: 자정(00:00)부터 점유 → 이후 시간만 반환
     *
     * 목적: 점유가 하루 시작(00:00)부터 시작될 때,
     * [MIN, MIN) 같은 길이 0인 구간 없이 이후 구간만 반환하는지 확인
     *
     * 점유: |━━━━━━━━━━━━|
     * 00:00 12:00
     * 가용: |━━━━━━━━━━━━|
     * 12:00 23:59
     */
    @Test
    void findAvailable_occupiedFromMidnight_returnsOnlyAfter() {
        TimeRange fromMidnight = new TimeRange(LocalTime.MIN, LocalTime.of(12, 0));

        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of(fromMidnight));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.MAX);
    }

    /**
     * TC-12: 하루 끝(MAX)까지 점유 → 이전 시간만 반환
     *
     * 목적: 점유가 하루 끝(23:59:59…)까지 이어질 때,
     * [MAX, MAX) 같은 빈 구간 없이 이전 구간만 반환하는지 확인
     *
     * 점유: |━━━━━━━━━━━━|
     * 12:00 23:59
     * 가용: |━━━━━━━━━━━━|
     * 00:00 12:00
     */
    @Test
    void findAvailable_occupiedToEndOfDay_returnsOnlyBefore() {
        TimeRange toEndOfDay = new TimeRange(LocalTime.of(12, 0), LocalTime.MAX);

        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of(toEndOfDay));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.MIN);
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(12, 0));
    }

    /**
     * TC-13: 하루 전체 점유 → 가용 없음 (빈 결과)
     *
     * 목적: [MIN, MAX]로 하루가 전부 막혀 있을 때 가용 구간이 0개인지 확인
     *
     * 점유: |━━━━━━━━━━━━━━━━━━━━━━━━|
     * 가용: (없음)
     */
    @Test
    void findAvailable_fullDayOccupied_returnsEmpty() {
        TimeRange fullDay = new TimeRange(LocalTime.MIN, LocalTime.MAX);

        List<TimeRange> result = timeRangeOverlapManager.findAvailableSchedules(Set.of(fullDay));

        assertThat(result).isEmpty();
    }
}
