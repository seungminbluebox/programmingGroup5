package kr.ac.dankook.group5.azit.schedule;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dankook.group5.azit.user.Member;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	<T> Set<T> findByMemberAndDate(Member member, LocalDate date, Class<T> type);

	<T> Set<T> findByMemberInAndDate(Collection<Member> members, LocalDate date,
			Class<T> type);

	Set<Schedule> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);

	Set<Schedule> findByMemberInAndDateBetween(Collection<Member> members, LocalDate startDate, LocalDate endDate);
}
