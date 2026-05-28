package kr.ac.dankook.group5.azit.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRangeTest {

    // [10:00, 12:00)
    private final TimeRange base = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));

    @Test
    void isOverlap_partialOverlapFromLeft() {
        // [09:00, 11:00] — base와 [10:00, 11:00] 겹침
        TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertThat(base.isOverlap(range)).isTrue();
    }

    @Test
    void isOverlap_partialOverlapFromRight() {
        // [11:00, 13:00] — base와 [11:00, 12:00] 겹침
        TimeRange range = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));
        assertThat(base.isOverlap(range)).isTrue();
    }

    @Test
    void isOverlap_contained() {
        // [10:30, 11:30] — base 안에 완전히 포함
        TimeRange range = new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30));
        assertThat(base.isOverlap(range)).isTrue();
    }

    @Test
    void isOverlap_contains() {
        // [09:00, 13:00] — base를 완전히 포함
        TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(13, 0));
        assertThat(base.isOverlap(range)).isTrue();
    }

    @Test
    void isOverlap_exactlySame() {
        TimeRange range = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertThat(base.isOverlap(range)).isTrue();
    }

    @Test
    void isOverlap_touchingAtEnd_noOverlap() {
        // [12:00, 14:00] — base의 끝과 정확히 맞닿음, 겹치는 시간 없음
        TimeRange range = new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertThat(base.isOverlap(range)).isFalse();
    }

    @Test
    void isOverlap_touchingAtStart_noOverlap() {
        // [08:00, 10:00) — base의 시작과 정확히 맞닿음, 겹치는 시간 없음
        TimeRange range = new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(base.isOverlap(range)).isFalse();
    }

    @Test
    void isOverlap_completelyBefore_noOverlap() {
        // [08:00, 09:00] — base보다 완전히 앞
        TimeRange range = new TimeRange(LocalTime.of(8, 0), LocalTime.of(9, 0));
        assertThat(base.isOverlap(range)).isFalse();
    }

    @Test
    void isOverlap_completelyAfter_noOverlap() {
        // [13:00, 14:00] — base보다 완전히 뒤
        TimeRange range = new TimeRange(LocalTime.of(13, 0), LocalTime.of(14, 0));
        assertThat(base.isOverlap(range)).isFalse();
    }

    @Test
    void isOverlap_oneSecondOverlap() {
        // [11:59, 13:00] — 단 1분만 겹침
        TimeRange range = new TimeRange(LocalTime.of(11, 59), LocalTime.of(13, 0));
        assertThat(base.isOverlap(range)).isTrue();
    }

    // --- isBefore(LocalTime) ---

    @Test
    void isBefore_localTime_endTimeStrictlyBefore() {
        // endTime(12:00) < 13:00 → true
        assertThat(base.isBefore(LocalTime.of(13, 0))).isTrue();
    }

    @Test
    void isBefore_localTime_endTimeEqual_false() {
        // endTime(12:00) == 12:00 → true [start, end)
        assertThat(base.isBefore(LocalTime.of(12, 0))).isTrue();
    }

    @Test
    void isBefore_localTime_endTimeAfter_false() {
        // endTime(12:00) > 11:00 → false
        assertThat(base.isBefore(LocalTime.of(11, 0))).isFalse();
    }

    // --- isBefore(TimeRange) ---

    @Test
    void isBefore_range_completelyBefore() {
        // base endTime(12:00) < range startTime(13:00) → true
        TimeRange range = new TimeRange(LocalTime.of(13, 0), LocalTime.of(14, 0));
        assertThat(base.isBefore(range)).isTrue();
    }

    @Test
    void isBefore_range_touching_false() {
        // base endTime(12:00) == range startTime(12:00) → true [start, end)
        TimeRange range = new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertThat(base.isBefore(range)).isTrue();
    }

    @Test
    void isBefore_range_overlapping_false() {
        TimeRange range = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));
        assertThat(base.isBefore(range)).isFalse();
    }

    // --- isAfter(LocalTime) ---

    @Test
    void isAfter_localTime_startTimeStrictlyAfter() {
        // startTime(10:00) > 09:00 → true
        assertThat(base.isAfter(LocalTime.of(9, 0))).isTrue();
    }

    @Test
    void isAfter_localTime_startTimeEqual_false() {
        // startTime(10:00) == 10:00 → false (strict >)
        assertThat(base.isAfter(LocalTime.of(10, 0))).isFalse();
    }

    @Test
    void isAfter_localTime_startTimeBefore_false() {
        // startTime(10:00) < 11:00 → false
        assertThat(base.isAfter(LocalTime.of(11, 0))).isFalse();
    }

    // --- isAfter(TimeRange) ---

    @Test
    void isAfter_range_completelyAfter() {
        // base startTime(10:00) > range endTime(09:00) → true
        TimeRange range = new TimeRange(LocalTime.of(7, 0), LocalTime.of(9, 0));
        assertThat(base.isAfter(range)).isTrue();
    }

    @Test
    void isAfter_range_touching_false() {
        // base startTime(10:00) == range endTime(10:00) → true [start, end)
        TimeRange range = new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(base.isAfter(range)).isTrue();
    }

    @Test
    void isAfter_range_overlapping_false() {
        TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertThat(base.isAfter(range)).isFalse();
    }

    // --- isBetween(LocalTime) ---

    @Test
    void isBetween_timeInsideRange() {
        // 11:00 은 [10:00, 12:00) 안에 있음
        assertThat(base.contains(LocalTime.of(11, 0))).isTrue();
    }

    @Test
    void isBetween_timeAtStart_false() {
        // startTime 경계는 포함 [start, end)
        assertThat(base.contains(LocalTime.of(10, 0))).isTrue();
    }

    @Test
    void isBetween_timeAtEnd_false() {
        // endTime 경계는 포함하지 않음 [start, end)
        assertThat(base.contains(LocalTime.of(12, 0))).isFalse();
    }

    @Test
    void isBetween_timeBefore_false() {
        assertThat(base.contains(LocalTime.of(9, 0))).isFalse();
    }

    @Test
    void isBetween_timeAfter_false() {
        assertThat(base.contains(LocalTime.of(13, 0))).isFalse();
    }

    // --- 상호 배타성: 임의 시점 t에 대해 isBefore/isBetween/isAfter 중 정확히 하나만 true ---

    private void assertExactlyOneTrue(LocalTime t) {
        int count = (base.isBefore(t) ? 1 : 0)
                + (base.contains(t) ? 1 : 0)
                + (base.isAfter(t) ? 1 : 0);
        assertThat(count).as("exactly one of isBefore/isBetween/isAfter at %s", t).isEqualTo(1);
    }

    @Test
    void exactlyOneTrue_at9() {
        assertExactlyOneTrue(LocalTime.of(9, 0));
    }

    @Test
    void exactlyOneTrue_at10() {
        assertExactlyOneTrue(LocalTime.of(10, 0));
    }

    @Test
    void exactlyOneTrue_at11() {
        assertExactlyOneTrue(LocalTime.of(11, 0));
    }

    @Test
    void exactlyOneTrue_at12() {
        assertExactlyOneTrue(LocalTime.of(12, 0));
    }

    @Test
    void exactlyOneTrue_at13() {
        assertExactlyOneTrue(LocalTime.of(13, 0));
    }

    // --- 상호 배타성: 임의 범위 r에 대해 isBefore/isOverlap/isAfter 중 정확히 하나만 true ---

    private void assertExactlyOneTrue(TimeRange r) {
        int count = (base.isBefore(r) ? 1 : 0)
                + (base.isOverlap(r) ? 1 : 0)
                + (base.isAfter(r) ? 1 : 0);
        assertThat(count).as("exactly one of isBefore/isOverlap/isAfter for %s", r).isEqualTo(1);
    }

    @Test
    void rangeExactlyOneTrue_completelyBefore() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(7, 0), LocalTime.of(9, 0)));
    }

    @Test
    void rangeExactlyOneTrue_touchingAtStart() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    void rangeExactlyOneTrue_overlapFromLeft() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0)));
    }

    @Test
    void rangeExactlyOneTrue_same() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void rangeExactlyOneTrue_overlapFromRight() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0)));
    }

    @Test
    void rangeExactlyOneTrue_touchingAtEnd() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0)));
    }

    @Test
    void rangeExactlyOneTrue_completelyAfter() {
        assertExactlyOneTrue(new TimeRange(LocalTime.of(13, 0), LocalTime.of(15, 0)));
    }

    // --- extend ---

    @Test
    void extend_partialOverlapFromLeft() {
        // [09:00, 11:00) + [10:00, 12:00) → [09:00, 12:00)
        TimeRange left = new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertThat(left.extend(base))
                .isEqualTo(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }

    @Test
    void extend_partialOverlapFromRight() {
        // [10:00, 12:00) + [11:00, 13:00) → [10:00, 13:00)
        TimeRange right = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));
        assertThat(base.extend(right))
                .isEqualTo(new TimeRange(LocalTime.of(10, 0), LocalTime.of(13, 0)));
    }

    @Test
    void extend_baseContainsRange() {
        // [10:00, 12:00) + [10:30, 11:30) → [10:00, 12:00) (base 유지)
        TimeRange inner = new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30));
        assertThat(base.extend(inner))
                .isEqualTo(new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void extend_rangeContainsBase() {
        // [10:00, 12:00) + [09:00, 13:00) → [09:00, 13:00)
        TimeRange outer = new TimeRange(LocalTime.of(9, 0), LocalTime.of(13, 0));
        assertThat(base.extend(outer))
                .isEqualTo(new TimeRange(LocalTime.of(9, 0), LocalTime.of(13, 0)));
    }

    @Test
    void extend_exactlySame() {
        // 동일 범위 extend → 자기 자신
        TimeRange same = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertThat(base.extend(same))
                .isEqualTo(new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void extend_nonOverlapping_throwsIllegalArgument() {
        // 겹치지 않는 범위 → IllegalArgumentException
        TimeRange after = new TimeRange(LocalTime.of(13, 0), LocalTime.of(14, 0));
        assertThatThrownBy(() -> base.extend(after))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extend_adjacent() {
        // [10:00, 12:00) + [12:00, 14:00) → [10:00, 14:00) — 인접은 이어붙일 수 있음
        TimeRange adjacent = new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertThat(base.extend(adjacent))
                .isEqualTo(new TimeRange(LocalTime.of(10, 0), LocalTime.of(14, 0)));
    }

    // --- isAdjacent ---

    @Test
    void isAdjacent_rightSide() {
        // base.endTime(12:00) == range.startTime(12:00) → true
        TimeRange right = new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertThat(base.isAdjacent(right)).isTrue();
    }

    @Test
    void isAdjacent_leftSide() {
        // base.startTime(10:00) == range.endTime(10:00) → true
        TimeRange left = new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(base.isAdjacent(left)).isTrue();
    }

    @Test
    void isAdjacent_overlapping_false() {
        // 겹치는 구간은 인접이 아님
        TimeRange overlapping = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));
        assertThat(base.isAdjacent(overlapping)).isFalse();
    }

    @Test
    void isAdjacent_gapBefore_false() {
        // 간격이 있는 앞쪽 구간
        TimeRange before = new TimeRange(LocalTime.of(7, 0), LocalTime.of(9, 0));
        assertThat(base.isAdjacent(before)).isFalse();
    }

    @Test
    void isAdjacent_gapAfter_false() {
        // 간격이 있는 뒤쪽 구간
        TimeRange after = new TimeRange(LocalTime.of(13, 0), LocalTime.of(15, 0));
        assertThat(base.isAdjacent(after)).isFalse();
    }

    @Test
    void isAdjacent_exactlySame_false() {
        // 동일한 범위는 인접이 아닌 overlap
        TimeRange same = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertThat(base.isAdjacent(same)).isFalse();
    }
}
