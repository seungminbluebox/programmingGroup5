package kr.ac.dankook.group5.azit.schedule.entity;

import jakarta.persistence.*;
import kr.ac.dankook.group5.azit.schedule.dto.DailySchedule;
import kr.ac.dankook.group5.azit.schedule.dto.TimeRange;
import kr.ac.dankook.group5.azit.user.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    public TimeRange toTimeRange() {
        return new TimeRange(startTime, endTime);
    }
}
