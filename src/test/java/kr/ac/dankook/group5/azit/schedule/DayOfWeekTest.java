package kr.ac.dankook.group5.azit.schedule;

import org.junit.jupiter.api.Test;

import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class DayOfWeekTest {

    @Test
    void fromBuiltin_convertsJavaDayOfWeekToProjectDayOfWeek() {
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.MONDAY)).isEqualTo(DayOfWeek.MON);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.TUESDAY)).isEqualTo(DayOfWeek.TUE);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.WEDNESDAY)).isEqualTo(DayOfWeek.WED);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.THURSDAY)).isEqualTo(DayOfWeek.THU);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.FRIDAY)).isEqualTo(DayOfWeek.FRI);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.SATURDAY)).isEqualTo(DayOfWeek.SAT);
        assertThat(DayOfWeek.fromBuiltin(java.time.DayOfWeek.SUNDAY)).isEqualTo(DayOfWeek.SUN);
    }
}
